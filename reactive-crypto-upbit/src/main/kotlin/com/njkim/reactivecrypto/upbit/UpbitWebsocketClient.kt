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

import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.upbit.model.UpbitOrderBook
import com.njkim.reactivecrypto.upbit.model.UpbitTickData
import io.netty.handler.codec.json.JsonObjectDecoder
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors
import kotlin.streams.toList

class UpbitWebsocketClient : ExchangeWebsocketClient {
    private val baseUri: String = "wss://api.upbit.com/websocket/v1"

    private val lastOrderBookTimestamp = AtomicLong()
    private val orderBookTimestampDuplicateCount = AtomicInteger()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        // CoinSymbol: {rightCurrency}-{leftCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.baseCurrency}-${currencyPair.targetCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .tcpConfiguration { t -> t.doOnConnected { it.addHandlerLast(JsonObjectDecoder()) } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"trade\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { UpbitJsonObjectMapper.instance.readValue(it, UpbitTickData::class.java) }
            .map {
                TickData(
                    it.sequentialId.toString() + it.code,
                    it.tradeTimestamp,
                    it.tradePrice,
                    it.tradeVolume,
                    it.code,
                    ExchangeVendor.UPBIT
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        // CoinSymbol: {targetCurrency}-{baseCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.baseCurrency}-${currencyPair.targetCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .tcpConfiguration { tcp -> tcp.doOnConnected { } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"orderbook\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { UpbitJsonObjectMapper.instance.readValue(it, UpbitOrderBook::class.java) }
            .map {
                OrderBook(
                    createOrderBookUniqueId(it.timestamp.toEpochMilli()),
                    it.code,
                    ZonedDateTime.now(),
                    ExchangeVendor.UPBIT,
                    it.orderBookUnits.stream()
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.bidPrice,
                                orderBookUnit.bidSize,
                                OrderSideType.ASK,
                                null
                            )
                        }
                        .toList(),
                    it.orderBookUnits.stream()
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.askPrice,
                                orderBookUnit.askSize,
                                OrderSideType.ASK,
                                null
                            )
                        }
                        .toList()
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