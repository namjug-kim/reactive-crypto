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

package com.njkim.reactivecrypto.idax

import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import mu.KotlinLogging
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class IdaxWebsocketClient : ExchangeWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val idaxRawWebsocketClient: IdaxRawWebsocketClient =
        IdaxRawWebsocketClient()

    private val lastTickDataTimestamp = AtomicLong()
    private val tickDataTimestampDuplicateCount = AtomicInteger()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return idaxRawWebsocketClient.createTradeDataFlux(subscribeTargets)
            .flatMapIterable { idaxMessageFrame ->
                idaxMessageFrame.data.map {
                    TickData(
                        it.transactionNumber,
                        it.eventDateTime,
                        it.price,
                        it.volume,
                        idaxMessageFrame.currencyPair,
                        ExchangeVendor.IDAX,
                        it.tradeSideType
                    )
                }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        return idaxRawWebsocketClient.createOrderBookChangeFlux(subscribeTargets)
            .flatMapIterable { idaxMessageFrame ->
                idaxMessageFrame.data.map {
                    OrderBook(
                        createTickDataUniqueId(it.timestamp.toEpochMilli()),
                        idaxMessageFrame.currencyPair,
                        it.timestamp,
                        ExchangeVendor.IDAX,
                        it.bids,
                        it.asks
                    )
                }
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
                    asks = askMap.values.sortedBy { orderBookUnit -> orderBookUnit.price },
                    bids = bidMap.values.sortedByDescending { orderBookUnit -> orderBookUnit.price }
                )
                currentOrderBookMap[currentOrderBook.currencyPair] = currentOrderBook
                currentOrderBook
            }
    }

    /**
     * add salt value for create unique Id
     * timestamp is not enough to use uniqueId
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