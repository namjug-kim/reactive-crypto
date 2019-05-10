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

package com.njkim.reactivecrypto.kraken

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.AbstractExchangeWebsocketClient
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.kraken.model.KrakenOrderBook
import com.njkim.reactivecrypto.kraken.model.KrakenSubscriptionStatus
import com.njkim.reactivecrypto.kraken.model.KrakenTickDataWrapper
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Kraken Websockets Public API Version 0.1.1
 *
 * All messages sent and received via WebSockets are encoded in JSON format
 * All floating point fields (including timestamps) are quoted to preserve precision.
 * Format of each tradeable pair is A/B, where A and B are ISO 4217-A3 for standardized assets and popular unique symbol if not standardized.
 * Timestamps should not be considered unique and not be considered as aliases for transaction ids. Also, the granularity of timestamps is not representative of transaction rates.
 *
 * @author namjug.kim
 *
 */
class KrakenWebsocketClient : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://ws.kraken.com"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return KrakenJsonObjectMapper()
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()
        val channelCurrencyPairMap: MutableMap<Int, CurrencyPair> = ConcurrentHashMap()

        val subscribeSymbols = subscribeTargets
            .map { "${it.targetCurrency.name}/${it.baseCurrency.name}".toUpperCase() }
            .map { "\"$it\"" }
            .joinToString(",", "[", "]")

        val subscribeMessage = "{" +
                "\"event\": \"subscribe\"," +
                "\"pair\": $subscribeSymbols," +
                "\"subscription\": {\"name\":\"book\"}" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .doOnNext {
                if (it.contains("\"event\":\"subscriptionStatus")) {
                    val subscribeStatus = objectMapper.readValue<KrakenSubscriptionStatus>(it)
                    channelCurrencyPairMap.put(subscribeStatus.channelID, subscribeStatus.pair)
                }
            }
            .filter { !it.contains("\"event\":\"") }
            .map { objectMapper.readValue<KrakenOrderBook>(it) }
            .map { krakenOrderBook ->
                val now = ZonedDateTime.now()
                OrderBook(
                    "$now",
                    channelCurrencyPairMap[krakenOrderBook.channelID]!!,
                    now,
                    ExchangeVendor.KRAKEN,
                    krakenOrderBook.bids.map { OrderBookUnit(it.price, it.volume, OrderSideType.BID, null) },
                    krakenOrderBook.asks.map { OrderBookUnit(it.price, it.volume, OrderSideType.ASK, null) }
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
                    asks = askMap.values.sortedBy { orderBookUnit -> orderBookUnit.price },
                    bids = bidMap.values.sortedByDescending { orderBookUnit -> orderBookUnit.price }
                )
                currentOrderBookMap[currentOrderBook.currencyPair] = currentOrderBook
                currentOrderBook
            }
            .doOnError { log.error(it.message, it) }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val channelCurrencyPairMap: MutableMap<Int, CurrencyPair> = ConcurrentHashMap()

        val subscribeSymbols = subscribeTargets
            .map { "${it.targetCurrency.name}/${it.baseCurrency.name}".toUpperCase() }
            .map { "\"$it\"" }
            .joinToString(",", "[", "]")

        val subscribeMessage = "{" +
                "\"event\": \"subscribe\"," +
                "\"pair\": $subscribeSymbols," +
                "\"subscription\": {\"name\":\"trade\"}" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .doOnNext {
                if (it.contains("\"event\":\"subscriptionStatus")) {
                    val subscribeStatus = objectMapper.readValue<KrakenSubscriptionStatus>(it)
                    channelCurrencyPairMap.put(subscribeStatus.channelID, subscribeStatus.pair)
                }
            }
            .filter { !it.contains("\"event\":\"") }
            .map { objectMapper.readValue<KrakenTickDataWrapper>(it) }
            .flatMapIterable {
                it.data.map { krakenTickData ->
                    TickData(
                        "${krakenTickData.time}", // FIXME time is not enough to use uniqueId
                        krakenTickData.time,
                        krakenTickData.price,
                        krakenTickData.volume,
                        channelCurrencyPairMap[it.channelId]!!,
                        ExchangeVendor.KRAKEN,
                        krakenTickData.tradeSideType
                    )
                }
            }
            .doOnError { log.error(it.message, it) }
    }
}
