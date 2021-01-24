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

package com.njkim.reactivecrypto.upbit

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
import com.njkim.reactivecrypto.upbit.model.UpbitOrderBook
import com.njkim.reactivecrypto.upbit.model.UpbitTickData
import io.netty.handler.codec.json.JsonObjectDecoder
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors

class UpbitWebsocketClient : AbstractExchangeWebsocketClient() {
    private val baseUri: String = "wss://api.upbit.com/websocket/v1"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    private val lastOrderBookTimestamp = AtomicLong()

    private val orderBookTimestampDuplicateCount = AtomicInteger()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return UpbitJsonObjectMapper()
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        // CoinSymbol: {rightCurrency}-{leftCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.quoteCurrency}-${currencyPair.baseCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .doOnConnected { it.addHandlerLast(JsonObjectDecoder()) }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"trade\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { objectMapper.readValue<UpbitTickData>(it) }
            .map {
                TickData(
                    it.sequentialId.toString() + it.code,
                    it.tradeTimestamp,
                    it.tradePrice,
                    it.tradeVolume,
                    it.code,
                    ExchangeVendor.UPBIT,
                    it.askBid
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        // CoinSymbol: {targetCurrency}-{baseCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.quoteCurrency}-${currencyPair.baseCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"orderbook\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { objectMapper.readValue<UpbitOrderBook>(it) }
            .map {
                OrderBook(
                    createOrderBookUniqueId(it.timestamp.toEpochMilli()),
                    it.code,
                    ZonedDateTime.now(),
                    ExchangeVendor.UPBIT,
                    it.orderBookUnits
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.bidPrice,
                                orderBookUnit.bidSize,
                                TradeSideType.BUY,
                                null
                            )
                        },
                    it.orderBookUnits
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.askPrice,
                                orderBookUnit.askSize,
                                TradeSideType.SELL,
                                null
                            )
                        }
                )
            }
    }

    /**
     * add salt value for create unique Id
     */
    private fun createOrderBookUniqueId(upbitTimestamp: Long): String {
        return if (lastOrderBookTimestamp.getAndSet(upbitTimestamp) == upbitTimestamp) {
            "${upbitTimestamp + orderBookTimestampDuplicateCount.incrementAndGet()}"
        } else {
            orderBookTimestampDuplicateCount.set(0)
            "$upbitTimestamp"
        }
    }
}
