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

package com.njkim.reactivecrypto.hubi

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.AbstractExchangeWebsocketClient
import com.njkim.reactivecrypto.hubi.model.HubiDepthResponse
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class HubiWebsocketClient : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://api.hubi.com/ws/futures/public/market"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return HubiJsonObjectMapper()
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        val subscribeRequests = subscribeTargets.asSequence()
            .map { "${it.baseCurrency.symbol}${it.quoteCurrency.symbol}".toUpperCase() }
            .map { symbol ->
                """{"op":"subscribe", "channel":"/api/depth/depth", "key":"$symbol"}"""
            }
            .toFlux()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket(WebsocketClientSpec.builder().maxFramePayloadLength(262144).build())
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains(""""event":"/api/depth/depth"""") }
            .map { objectMapper.readValue<HubiDepthResponse>(it) }
            .map { messageFrame ->
                val eventTime = ZonedDateTime.now()
                OrderBook(
                    "${messageFrame.key}${eventTime.toEpochMilli()}",
                    messageFrame.key,
                    eventTime,
                    ExchangeVendor.HUBI,
                    messageFrame.buyDepth.map { OrderBookUnit(it.price, it.qty, TradeSideType.BUY, it.count) },
                    messageFrame.sellDepth.map { OrderBookUnit(it.price, it.qty, TradeSideType.SELL, it.count) }
                        .sortedBy { it.price }
                )
            }
            .map { orderBook ->
                if (!currentOrderBookMap.containsKey(orderBook.currencyPair)) {
                    val filteredOrderBook = orderBook.copy(
                        bids = orderBook.bids.filter { it.quantity > BigDecimal.ZERO },
                        asks = orderBook.asks.filter { it.quantity > BigDecimal.ZERO }
                    )
                    currentOrderBookMap[orderBook.currencyPair] = filteredOrderBook
                    return@map filteredOrderBook
                }

                val prevOrderBook = currentOrderBookMap[orderBook.currencyPair]!!

                val askMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.asks
                    .associateBy { it.price.stripTrailingZeros() }
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
                    .associateBy { it.price.stripTrailingZeros() }
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

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val lastPublishedTimestamp: MutableMap<CurrencyPair, AtomicLong> = ConcurrentHashMap()

        val subscribeRequests = subscribeTargets.asSequence()
            .map { "${it.baseCurrency.symbol}${it.quoteCurrency.symbol}".toUpperCase() }
            .map { symbol ->
                """{"op":"subscribe", "channel":"/api/depth/depth", "key":"$symbol"}"""
            }
            .toFlux()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket(WebsocketClientSpec.builder().maxFramePayloadLength(262144).build())
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains(""""event":"/api/depth/depth"""") }
            .map { objectMapper.readValue<HubiDepthResponse>(it) }
            .flatMapIterable {
                it.trades
                    .takeWhile { hubiTickData ->
                        // hubi trade history response contain history data
                        val lastTradeEpochMilli =
                            lastPublishedTimestamp.computeIfAbsent(hubiTickData.symbol) { AtomicLong() }
                        val isNew = hubiTickData.timestamp.toEpochMilli() > lastTradeEpochMilli.toLong()
                        if (isNew) {
                            lastTradeEpochMilli.set(hubiTickData.timestamp.toEpochMilli())
                        }
                        isNew
                    }
                    .map { hubiTickData ->
                        TickData(
                            "${hubiTickData.symbol}${hubiTickData.timestamp}",
                            hubiTickData.timestamp,
                            hubiTickData.price,
                            hubiTickData.qty,
                            hubiTickData.symbol,
                            ExchangeVendor.HUBI,
                            if (hubiTickData.buyActive) TradeSideType.BUY else TradeSideType.SELL
                        )
                    }
                    .reversed()
            }
            .doOnError { log.error(it.message, it) }
    }
}
