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
import com.njkim.reactivecrypto.hubi.model.HubiMessageFrame
import com.njkim.reactivecrypto.hubi.model.HubiOrderBook
import com.njkim.reactivecrypto.hubi.model.HubiTickDataWrapper
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

class HubiWebsocketClient : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://api.hubi.com/ws/connect/v1"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return HubiJsonObjectMapper()
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeRequests = subscribeTargets
            .map { "${it.targetCurrency.symbol}${it.baseCurrency.symbol}".toLowerCase() }
            .map { symbol ->
                """
                    {"channel":"depth_all","symbol":"$symbol"}
                """.trimIndent()
            }
            .toFlux()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket(262144)
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains("\"dataType\":\"depth_all\"") }
            .map { objectMapper.readValue<HubiMessageFrame<HubiOrderBook>>(it) }
            .map { messageFrame ->
                val eventTime = ZonedDateTime.now()
                OrderBook(
                    "${messageFrame.symbol}${eventTime.toEpochMilli()}",
                    messageFrame.symbol,
                    eventTime,
                    ExchangeVendor.HUBI,
                    messageFrame.data.bids.map { OrderBookUnit(it.price, it.amount, TradeSideType.BUY, null) },
                    messageFrame.data.asks.map { OrderBookUnit(it.price, it.amount, TradeSideType.SELL, null) }.sortedBy { it.price }
                )
            }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val lastPublishedTimestamp: MutableMap<CurrencyPair, AtomicLong> = ConcurrentHashMap()

        val subscribeRequests = subscribeTargets
            .map { "${it.targetCurrency.symbol}${it.baseCurrency.symbol}".toLowerCase() }
            .map { symbol ->
                """
                    {"channel":"trade_history","symbol":"$symbol"}
                """.trimIndent()
            }
            .toFlux()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket(65536)
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.aggregateFrames(65536).receive().asString())
            }
            .filter { it.contains("\"dataType\":\"trade_history\"") }
            .map { objectMapper.readValue<HubiMessageFrame<HubiTickDataWrapper>>(it) }
            .map { it.data }
            .flatMapIterable {
                it.trades
                    .takeWhile { hubiTickData ->
                        // hubi trade history response contain history data
                        val lastTradeEpochMilli =
                            lastPublishedTimestamp.computeIfAbsent(hubiTickData.symbol) { AtomicLong() }
                        val isNew = hubiTickData.time.toEpochMilli() > lastTradeEpochMilli.toLong()
                        if (isNew) {
                            lastTradeEpochMilli.set(hubiTickData.time.toEpochMilli())
                        }
                        isNew
                    }
                    .map { hubiTickData ->
                        TickData(
                            "${hubiTickData.symbol}${hubiTickData.time}",
                            hubiTickData.time,
                            hubiTickData.price,
                            hubiTickData.amount,
                            hubiTickData.symbol,
                            ExchangeVendor.HUBI,
                            hubiTickData.type
                        )
                    }
                    .reversed()
            }
            .doOnError { log.error(it.message, it) }
    }
}
