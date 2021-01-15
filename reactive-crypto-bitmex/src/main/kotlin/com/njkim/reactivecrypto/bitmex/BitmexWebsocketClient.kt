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

package com.njkim.reactivecrypto.bitmex

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bitmex.model.BitmexMessageFrame
import com.njkim.reactivecrypto.bitmex.model.BitmexOrderBook
import com.njkim.reactivecrypto.bitmex.model.BitmexTickData
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.AbstractExchangeWebsocketClient
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.Duration

class BitmexWebsocketClient : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://www.bitmex.com/realtime"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return BitmexJsonObjectMapper()
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val args = subscribeTargets.map { "\"orderBook10:${it.baseCurrency}${it.quoteCurrency}\"" }
            .joinToString(",", "[", "]")

        val subscribeMessage = "{\"op\": \"subscribe\", \"args\": $args}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        BitmexHeartbetsHandler(Duration.ofMillis(5000))
                    )
                }
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { it.contains("\"table\":\"orderBook10\"") }
            .map { objectMapper.readValue<BitmexMessageFrame<List<BitmexOrderBook>>>(it) }
            .flatMapIterable { messageFrame ->
                messageFrame.data.map { bitmexOrderBook ->
                    OrderBook(
                        "${bitmexOrderBook.symbol}${bitmexOrderBook.timestamp.toEpochMilli()}",
                        bitmexOrderBook.symbol,
                        bitmexOrderBook.timestamp,
                        ExchangeVendor.BITMEX,
                        bitmexOrderBook.getBids(),
                        bitmexOrderBook.getAsks()
                    )
                }
            }
            .doOnError { log.error(it.message, it) }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val args = subscribeTargets.map { "\"trade:${it.baseCurrency}${it.quoteCurrency}\"" }
            .joinToString(",", "[", "]")

        val subscribeMessage = "{\"op\": \"subscribe\", \"args\": $args}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        BitmexHeartbetsHandler(Duration.ofMillis(5000))
                    )
                }
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { it.contains("\"table\":\"trade\"") }
            .map { objectMapper.readValue<BitmexMessageFrame<List<BitmexTickData>>>(it) }
            .flatMapIterable {
                it.data
                    .map { bitmexTickData ->
                        TickData(
                            bitmexTickData.trdMatchID,
                            bitmexTickData.timestamp,
                            bitmexTickData.price,
                            bitmexTickData.size,
                            bitmexTickData.symbol,
                            ExchangeVendor.BITMEX,
                            bitmexTickData.side
                        )
                    }
            }
            .doOnError { log.error(it.message, it) }
    }
}
