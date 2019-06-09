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

package com.njkim.reactivecrypto.bitmax

import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.scheduler.Schedulers
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class BitmaxWebsocketClient : ExchangeWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val bitmaxRawWebsocketClient: BitmaxRawWebsocketClient = BitmaxRawWebsocketClient()

    private val lastTickDataTimestamp = AtomicLong()
    private val tickDataTimestampDuplicateCount = AtomicInteger()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val targetWebsockets = subscribeTargets.map {
            bitmaxRawWebsocketClient.createTradeDataFlux(it, 1)
                .publishOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
        }

        return Flux.merge(targetWebsockets)
            .flatMapIterable { bitmaxTickDataWrapper ->
                bitmaxTickDataWrapper.trades
                    .map {
                        TickData(
                            createTickDataUniqueId(it.timestamp.toEpochSecond()),
                            it.timestamp,
                            it.price,
                            it.quantity,
                            bitmaxTickDataWrapper.s,
                            ExchangeVendor.BITMAX,
                            if (it.bm) TradeSideType.SELL else TradeSideType.BUY
                        )
                    }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        val targetWebsockets = subscribeTargets.map {
            bitmaxRawWebsocketClient.createOrderBookFlux(it, 20)
                .publishOn(Schedulers.fromExecutor(Executors.newSingleThreadExecutor()))
        }

        return Flux.merge(targetWebsockets)
            .map { bitmaxOrderBookDataWrapper ->
                val now = ZonedDateTime.now()
                OrderBook(
                    "${bitmaxOrderBookDataWrapper.seqnum}",
                    bitmaxOrderBookDataWrapper.s,
                    now,
                    ExchangeVendor.BITMAX,
                    bitmaxOrderBookDataWrapper.bids.map {
                        OrderBookUnit(
                            it.price,
                            it.quantity,
                            OrderSideType.BID,
                            null
                        )
                    },
                    bitmaxOrderBookDataWrapper.asks.map {
                        OrderBookUnit(
                            it.price,
                            it.quantity,
                            OrderSideType.ASK,
                            null
                        )
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

    /**
     * add salt value for create unique Id
     */
    private fun createTickDataUniqueId(timestamp: Long): String {
        return if (lastTickDataTimestamp.getAndSet(timestamp) == timestamp) {

            val saltString = "${tickDataTimestampDuplicateCount.incrementAndGet()}".padStart(3, '0')
            "$timestamp$saltString"
        } else {
            tickDataTimestampDuplicateCount.set(0)
            val saltString = "${tickDataTimestampDuplicateCount.get()}".padStart(3, '0')
            "$timestamp$saltString"
        }
    }
}
