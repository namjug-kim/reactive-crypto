/*
 * Copyright 2019 namjug-kim
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.njkim.reactivecrypto.kucoin

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawHttpClient
import com.njkim.reactivecrypto.kucoin.model.KucoinOrderBookSnapshotResponse
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

/**
 * docs : https://docs.kucoin.com/#websocket-feed
 */
class KucoinWebsocketClient(
    private val apiScheme: String = "https",
    private val apiHost: String = "api.kucoin.com"
) : ExchangeWebsocketClient {
    private val kucoinRawWebsocketClient = KucoinRawWebsocketClient(apiScheme, apiHost)

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return kucoinRawWebsocketClient.createMatchExecutionData(subscribeTargets)
            .map { it.data }
            .map {
                TickData(
                    it.tradeId,
                    it.time,
                    it.price,
                    it.size,
                    it.symbol,
                    ExchangeVendor.KUCOIN,
                    it.side
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, KucoinOrderBookSnapshotResponse> = ConcurrentHashMap()
        val snapshotLoaded: MutableSet<CurrencyPair> = HashSet()

        // 1. connect level2 market data flux
        val marketLevel2Flux = kucoinRawWebsocketClient.createLevel2MarketDataFlux(subscribeTargets)
            .map { it.data }
            .publish()
            .autoConnect(1)
            .doFinally {
                snapshotLoaded.clear()
                currentOrderBookMap.clear()
            }

        // 2. call market snapshot using rest api
        marketLevel2Flux
            .publishOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))

            .filter { snapshotLoaded.add(it.symbol) }
            .flatMapIterable { subscribeTargets }
            .delayElements(Duration.ofMillis(500))
            .flatMap { currencyPair ->
                KucoinRawHttpClient(apiScheme, apiHost)
                    .publicApi()
                    .market()
                    .getFullOrderBook(currencyPair)
                    .doOnNext { currentOrderBookMap[currencyPair] = it }
                    .doOnError { snapshotLoaded.remove(currencyPair) }
            }
            .subscribe()

        // 3. apply change
        return marketLevel2Flux
            .filter { currentOrderBookMap[it.symbol] != null }
            .filter { marketLevel2 ->
                val currentOrderBook = currentOrderBookMap[marketLevel2.symbol]!!
                marketLevel2.sequenceStart > currentOrderBook.sequence
            }
            .map { marketLevel2 ->
                val currentOrderBook = currentOrderBookMap[marketLevel2.symbol]!!

                val bidMap = currentOrderBook.bids
                    .map { it.price.stripTrailingZeros() to it }
                    .toMap()
                    .toMutableMap()
                marketLevel2.changes.bids
                    .forEach { bidChange ->
                        if (bidChange.sequence != null && bidChange.sequence > currentOrderBook.sequence) {
                            if (bidChange.quantity <= BigDecimal.ZERO) {
                                bidMap.remove(bidChange.price.stripTrailingZeros())
                            } else {
                                bidMap[bidChange.price.stripTrailingZeros()] = bidChange
                            }
                        }
                    }

                val askMap = currentOrderBook.asks
                    .map { it.price.stripTrailingZeros() to it }
                    .toMap()
                    .toMutableMap()
                marketLevel2.changes.asks
                    .forEach { askChange ->
                        if (askChange.sequence != null && askChange.sequence > currentOrderBook.sequence) {
                            if (askChange.quantity <= BigDecimal.ZERO) {
                                askMap.remove(askChange.price.stripTrailingZeros())
                            } else {
                                askMap[askChange.price.stripTrailingZeros()] = askChange
                            }
                        }
                    }

                val updatedOrderBook = currentOrderBook.copy(
                    sequence = marketLevel2.sequenceEnd,
                    bids = bidMap.values
                        .sortedByDescending { it.price }
                        .toList(),
                    asks = askMap.values.toList()
                        .sortedBy { it.price }
                        .toList()
                )
                currentOrderBookMap[marketLevel2.symbol] = updatedOrderBook

                OrderBook(
                    updatedOrderBook.sequence.toPlainString(),
                    marketLevel2.symbol,
                    ZonedDateTime.now(),
                    ExchangeVendor.KUCOIN,
                    updatedOrderBook.bids.map { bid ->
                        OrderBookUnit(
                            bid.price,
                            bid.quantity,
                            TradeSideType.BUY
                        )
                    },
                    updatedOrderBook.asks.map { ask ->
                        OrderBookUnit(
                            ask.price,
                            ask.quantity,
                            TradeSideType.SELL
                        )
                    }
                )
            }
    }
}
