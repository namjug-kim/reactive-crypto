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

package com.njkim.reactivecrypto.kucoin

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawHttpClient
import com.njkim.reactivecrypto.kucoin.model.KucoinMarketLevel2
import com.njkim.reactivecrypto.kucoin.model.KucoinMatchExecutionData
import com.njkim.reactivecrypto.kucoin.model.KucoinMessageFrame
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.time.Instant
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * docs : https://docs.kucoin.com/#websocket-feed
 */
class KucoinRawWebsocketClient(
    private val apiScheme: String = "https",
    private val apiHost: String = "api.kucoin.com"
) {
    private val log = KotlinLogging.logger { }

    companion object {
        val objectMapper: ObjectMapper = KucoinJsonObjectMapper().objectMapper()
    }

    /**
     * Level-2 Market Data
     *
     * Topic: /market/level2:{symbol},{symbol}...
     *
     * 1. After receiving the websocket Level 2 data flow, cache the data.
     * 2. Initiate a REST request to get the snapshot data of Level 2 order book.
     * 3. Playback the cached Level 2 data flow.
     * 4. Apply the new Level 2 data flow to the local snapshot to ensure that the sequence of the new Level 2 update lines up with the sequence of the previous Level 2 data.
     *    Discard all the message prior to that sequence, and then playback the change to snapshot.
     * 5. Update the level2 full data based on sequence according to the size.
     *    If the price is 0, ignore the messages and update the sequence.
     *    If the size=0, update the sequence and remove the price of which the size is 0 out of level 2.
     *    For other cases, please update the price.
     */
    fun createLevel2MarketDataFlux(symbols: List<CurrencyPair>): Flux<KucoinMessageFrame<KucoinMarketLevel2>> {
        val markets = symbols.joinToString(",") { "${it.targetCurrency}-${it.baseCurrency}" }

        val subscribeRequest =
            Mono.create<String> { it.success("{\"id\":${Instant.now().toEpochMilli()},\"type\":\"subscribe\",\"topic\":\"/market/level2:$markets\"}") }

        return KucoinRawHttpClient(apiScheme, apiHost)
            .publicApi()
            .auth()
            .websocketAuth()
            .flatMapMany { websocketAuth ->
                val kucoinInstantServer = websocketAuth.instanceServers[0]

                HttpClient.create()
                    .tcpConfiguration { tcp ->
                        tcp.doOnConnected { connection ->
                            connection.addHandler(
                                "heartBeat",
                                HeartBeatHandler(
                                    false,
                                    kucoinInstantServer.pingInterval.toLong(),
                                    TimeUnit.MILLISECONDS,
                                    kucoinInstantServer.pingTimeout.toLong()
                                ) { "{\"id\":\"${ZonedDateTime.now().toEpochMilli()}\",\"type\":\"ping\"}" }
                            )
                        }
                    }
                    .wiretap(log.isDebugEnabled)
                    .websocket()
                    .uri(kucoinInstantServer.endpoint + "?token=${websocketAuth.token}")
                    .handle { inbound, outbound ->
                        outbound.sendString(subscribeRequest)
                            .then()
                            .thenMany(inbound.receive().asString())
                    }
                    .filter { it.contains("\"type\":\"message\"") }
                    .map { objectMapper.readValue<KucoinMessageFrame<KucoinMarketLevel2>>(it) }
            }
    }

    /**
     * Topic: /market/match:{symbol},{symbol}...
     *
     * Subscribe to this topic to obtain the matching event data flow of Level 3.
     * For each order traded, the system would send you the match messages in the following format.
     */
    fun createMatchExecutionData(symbols: List<CurrencyPair>): Flux<KucoinMessageFrame<KucoinMatchExecutionData>> {
        val markets = symbols.joinToString(",") { "${it.targetCurrency}-${it.baseCurrency}" }

        val subscribeRequest =
            Mono.create<String> { it.success("{\"id\":${Instant.now().toEpochMilli()},\"type\":\"subscribe\",\"topic\":\"/market/match:$markets\"}") }

        return KucoinRawHttpClient(apiScheme, apiHost)
            .publicApi()
            .auth()
            .websocketAuth()
            .flatMapMany { websocketAuth ->
                val kucoinInstantServer = websocketAuth.instanceServers[0]

                HttpClient.create()
                    .tcpConfiguration { tcp ->
                        tcp.doOnConnected { connection ->
                            connection.addHandler(
                                "heartBeat",
                                HeartBeatHandler(
                                    false,
                                    kucoinInstantServer.pingInterval.toLong(),
                                    TimeUnit.MILLISECONDS,
                                    kucoinInstantServer.pingTimeout.toLong()
                                ) { "{\"id\":\"${ZonedDateTime.now().toEpochMilli()}\",\"type\":\"ping\"}" }
                            )
                        }
                    }
                    .wiretap(log.isDebugEnabled)
                    .websocket()
                    .uri(kucoinInstantServer.endpoint + "?token=${websocketAuth.token}")
                    .handle { inbound, outbound ->
                        outbound.sendString(subscribeRequest)
                            .then()
                            .thenMany(inbound.receive().asString())
                    }
                    .filter { it.contains("\"type\":\"message\"") }
                    .map { objectMapper.readValue<KucoinMessageFrame<KucoinMatchExecutionData>>(it) }
            }
    }
}
