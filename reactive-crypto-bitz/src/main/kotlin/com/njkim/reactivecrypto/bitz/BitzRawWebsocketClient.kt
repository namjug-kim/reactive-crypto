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

package com.njkim.reactivecrypto.bitz

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bitz.model.BitzMessageFrame
import com.njkim.reactivecrypto.bitz.model.BitzOrderBook
import com.njkim.reactivecrypto.bitz.model.BitzTradeData
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.time.Instant
import java.util.concurrent.TimeUnit

class BitzRawWebsocketClient(
    host: String = "wsapi.bitz.top",
    private val cdid: String = "100002"
) {
    private val log = KotlinLogging.logger {}

    private val uri: String = "wss://$host/"

    companion object {
        val objectMapper: ObjectMapper = BitzJsonObjectMapper().objectMapper()
    }

    /**
     * Depth information :
     *
     * {
     * "msgId": 0, #message id
     * "params": { # parameter
     * "symbol": "bz_usdt" #trading pair
     * },
     * "action": "Pushdata.depth", #Subscribe type
     * "data": { #data
     * "asks": [ # Entrusted direction: asks: sell, bids: buy
     * [
     * "0.1586", #price
     * "616.4175", #Quantity
     * "97.7638" #Total amount
     * ]
     * ]
     * },
     * "time": 1562159910917, #Message time
     * "source": "sub-api" #Message source
     * }
     */
    fun createDepthFlux(symbols: List<CurrencyPair>): Flux<BitzMessageFrame<BitzOrderBook>> {
        val subscribeMessage = symbols.map { "${it.targetCurrency}_${it.baseCurrency}".toLowerCase() }
            .toFlux()
            .map { "{\"action\":\"Topic.sub\",\"data\":{\"symbol\":\"$it\",\"type\":\"depth\",\"_CDID\":\"$cdid\"},\"msg_id\":${Instant.now().toEpochMilli()}}" }

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            5000,
                            TimeUnit.MILLISECONDS,
                            1000
                        ) { "ping" }
                    )
                }
            }
            .websocket()
            .uri(uri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeMessage)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it != "pong" }
            .map { objectMapper.readValue<BitzMessageFrame<BitzOrderBook>>(it) }
            .map { bitzMessageFrame ->
                val bitzOrderBook = bitzMessageFrame.data
                bitzMessageFrame.copy(
                    data = bitzOrderBook.copy(
                        nullableBids = bitzOrderBook.bids.sortedByDescending { it.price },
                        nullableAsks = bitzOrderBook.asks.sortedBy { it.price }
                    )
                )
            }
    }

    fun createTradeFlux(symbols: List<CurrencyPair>): Flux<BitzMessageFrame<List<BitzTradeData>>> {
        val subscribeMessage = symbols.map { "${it.targetCurrency}_${it.baseCurrency}".toLowerCase() }
            .toFlux()
            .map { "{\"action\":\"Topic.sub\",\"data\":{\"symbol\":\"$it\",\"type\":\"order\",\"_CDID\":\"$cdid\"},\"msg_id\":${Instant.now().toEpochMilli()}}" }

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            5000,
                            TimeUnit.MILLISECONDS,
                            1000
                        ) { "ping" }
                    )
                }
            }
            .websocket()
            .uri(uri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeMessage)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { it != "pong" }
            .map { objectMapper.readValue<BitzMessageFrame<List<BitzTradeData>>>(it) }
    }
}