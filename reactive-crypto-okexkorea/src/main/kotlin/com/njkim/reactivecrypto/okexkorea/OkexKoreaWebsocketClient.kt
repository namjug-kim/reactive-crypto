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

package com.njkim.reactivecrypto.okexkorea

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Okex Korea is based on OKEX. but it has different interface than OKEX websocket
 */
class OkexKoreaWebsocketClient : ExchangeWebsocketClient {
    private val okexKoreaRawWebsocketClient = OkexKoreaRawWebsocketClient()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return okexKoreaRawWebsocketClient.createTickDataFlux(subscribeTargets)
            .flatMapIterable { messageFrame ->
                messageFrame.data
                    .map {
                        TickData(
                            it.uniqueId,
                            LocalDateTime.of(LocalDate.now(), it.eventTime).atZone(ZoneId.systemDefault()),
                            it.price,
                            it.quantity,
                            messageFrame.currencyPair,
                            ExchangeVendor.OKEX_KOREA,
                            it.side
                        )
                    }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        return okexKoreaRawWebsocketClient.createDepthFlux(subscribeTargets)
            .map {
                val now = ZonedDateTime.now()

                OrderBook(
                    "${now.toEpochMilli()}",
                    CurrencyPair(it.base, it.quote),
                    now,
                    ExchangeVendor.OKEX_KOREA,
                    it.data.bids.map { bid ->
                        OrderBookUnit(bid.price, bid.totalSize, TradeSideType.BUY)
                    },
                    it.data.asks.map { ask ->
                        OrderBookUnit(ask.price, ask.totalSize, TradeSideType.SELL)
                    }
                )
            }
            .map { orderBook ->
                if (!currentOrderBookMap.containsKey(orderBook.currencyPair)) {
                    currentOrderBookMap[orderBook.currencyPair] = orderBook
                    return@map orderBook
                }

                val prevOrderBook = currentOrderBookMap[orderBook.currencyPair]!!

                val askMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.asks
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.asks.forEach { updatedAsk ->
                    askMap.compute(updatedAsk.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedAsk.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedAsk
                            else -> oldValue.copy(
                                quantity = updatedAsk.quantity,
                                orderNumbers = updatedAsk.orderNumbers
                            )
                        }
                    }
                }

                val bidMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.bids
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.bids.forEach { updatedBid ->
                    bidMap.compute(updatedBid.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedBid.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedBid
                            else -> oldValue.copy(
                                quantity = updatedBid.quantity,
                                orderNumbers = updatedBid.orderNumbers
                            )
                        }
                    }
                }

                val currentOrderBook = prevOrderBook.copy(
                    eventTime = orderBook.eventTime,
                    asks = askMap.values.sortedBy { orderBookUnit -> orderBookUnit.price },
                    bids = bidMap.values.sortedByDescending { orderBookUnit -> orderBookUnit.price }
                )
                currentOrderBookMap[currentOrderBook.currencyPair] = currentOrderBook
                currentOrderBook
            }
            .doFinally { currentOrderBookMap.clear() } // cleanup memory limit orderBook when disconnected
    }
}
