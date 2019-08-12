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

package com.njkim.reactivecrypto.bhex

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bhex.model.BhexMessageFrame
import com.njkim.reactivecrypto.bhex.model.BhexOrderBook
import com.njkim.reactivecrypto.bhex.model.BhexTickData
import com.njkim.reactivecrypto.bhex.model.BhexTicker
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

class BhexRawWebsocketClient(
    host: String = "ws.bhex.com"
) {
    private val log = KotlinLogging.logger {}

    private val uri: String = "wss://$host/openapi/quote/ws/v1"

    companion object {
        val objectMapper: ObjectMapper = BhexJsonObjectMapper().objectMapper()
    }

    /**
     * - The book dump frequency：Every 300ms, if book version changed.
     * - The book dump depth：300 for asks and bids each.
     * - The book version change event：
     *    - order enters book
     *    - order leaves book
     *    - order quantity or amount changes
     *    - order is finished
     */
    fun createDepthFlux(symbols: List<CurrencyPair>): Flux<BhexMessageFrame<List<BhexOrderBook>>> {
        val subscribeSymbols = symbols.joinToString(",") { "${it.targetCurrency}${it.baseCurrency}" }
        val topic = "depth"

        val subscribeMessage = "{" +
                "\"symbol\": \"$subscribeSymbols\"," +
                "\"topic\": \"$topic\"," +
                "\"event\": \"sub\"" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            10500,
                            TimeUnit.MILLISECONDS,
                            1000
                        ) { "ping" }
                    )
                }
            }
            .websocket()
            .uri(uri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains("\"topic\":\"$topic\"") }
            .map { objectMapper.readValue<BhexMessageFrame<List<BhexOrderBook>>>(it) }
    }

    /**
     * 24hr Ticker statistics for a symbol that changed in an array.
     */
    fun createTickersFlux(symbols: List<CurrencyPair>): Flux<BhexMessageFrame<List<BhexTicker>>> {
        val subscribeSymbols = symbols.joinToString(",") { "${it.targetCurrency}${it.baseCurrency}" }
        val topic = "realtimes"

        val subscribeMessage = "{" +
                "\"symbol\": \"$subscribeSymbols\"," +
                "\"topic\": \"$topic\"," +
                "\"event\": \"sub\"" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            10500,
                            TimeUnit.MILLISECONDS,
                            1000
                        ) { "ping" }
                    )
                }
            }
            .websocket()
            .uri(uri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains("\"topic\":\"$topic\"") }
            .map { objectMapper.readValue<BhexMessageFrame<List<BhexTicker>>>(it) }
    }

    /**
     * The Trade Streams push raw trade information;
     * each trade has a unique buyer and seller.
     * After the first successful subscription, system will return 60 historical trades, then will return real-time trades afterwards.
     * Variable "v" could be used to determine the version of the data.
     * ("v" will be increasing, but not in a continuous sense, namely v(n + 1) might not be v(n) + 1, and v(n + 1) > v(n) for sure)
     */
    fun createTradeFlux(symbols: List<CurrencyPair>): Flux<BhexMessageFrame<List<BhexTickData>>> {
        val subscribeSymbols = symbols.joinToString(",") { "${it.targetCurrency}${it.baseCurrency}" }
        val topic = "trade"

        val subscribeMessage = "{" +
                "\"symbol\": \"$subscribeSymbols\"," +
                "\"topic\": \"$topic\"," +
                "\"event\": \"sub\"" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            10500,
                            TimeUnit.MILLISECONDS,
                            1000
                        ) { "ping" }
                    )
                }
            }
            .websocket()
            .uri(uri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it.contains("\"topic\":\"$topic\"") }
            .map { objectMapper.readValue<BhexMessageFrame<List<BhexTickData>>>(it) }
    }
}