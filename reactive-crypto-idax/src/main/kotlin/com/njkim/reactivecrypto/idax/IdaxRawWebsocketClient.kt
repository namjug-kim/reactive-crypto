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

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import com.njkim.reactivecrypto.idax.model.IdaxMessageFrame
import com.njkim.reactivecrypto.idax.model.IdaxOrderBook
import com.njkim.reactivecrypto.idax.model.IdaxTickData
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.util.concurrent.TimeUnit

class IdaxRawWebsocketClient(
    private val baseUri: String = "wss://openws.idax.pro/ws"
) {
    private val log = KotlinLogging.logger {}

    companion object {
        val objectMapper: ObjectMapper = IdaxJsonObjectMapper().objectMapper()
    }

    /**
     * @param symbols trade pairs for subscribe tick data
     *
     * idax_sub_X_trades  Subscription transaction record (incremental data return)
     *
     * response example
     * {
     * "channel":"idax_sub_eth_btc_trades",
     * "data":[["1001","2463.86","0.052",1411718972024,"buy"]]
     * }
     *
     */
    fun createTradeDataFlux(symbols: List<CurrencyPair>): Flux<IdaxMessageFrame<List<IdaxTickData>>> {
        val subscribeStrings = symbols.map { "${it.baseCurrency}_${it.quoteCurrency}".toLowerCase() }
            .map { "{'event':'addChannel','channel':'idax_sub_${it}_trades'}" }
            .toFlux()

        return HttpClient.create()
            .doOnConnected {
                it.addHandlerFirst(
                    "heartBeat",
                    HeartBeatHandler(false, 2, TimeUnit.SECONDS, 1) { "{\"event\":\"ping\"}" }
                )
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeStrings)
                    .then()
                    .thenMany(inbound.receive().asString())
                    .filter { response ->
                        !response!!.contentEquals("{\"event\":\"pong\"}")
                    }
            }
            .map { objectMapper.readValue<IdaxMessageFrame<List<IdaxTickData>>>(it) }
    }

    /**
     * @param symbols trade pairs for subscribe tick data
     *
     * idax_sub_X_depth  Subscribe to market depth (200 incremental data return)
     *
     * After the first return of the total data (<=200), the first return of the data is performed
     * according to the server data in the following three operations Delete (when the quantity is 0)
     * Modification (same price, different quantity) Increase (price does not exist)
     */
    fun createOrderBookChangeFlux(symbols: List<CurrencyPair>): Flux<IdaxMessageFrame<List<IdaxOrderBook>>> {
        val subscribeStrings = symbols.map { "${it.baseCurrency}_${it.quoteCurrency}".toLowerCase() }
            .map { "{'event':'addChannel','channel':'idax_sub_${it}_depth'}" }
            .toFlux()

        return HttpClient.create()
            .doOnConnected {
                it.addHandlerFirst(
                    "heartBeat",
                    HeartBeatHandler(false, 2, TimeUnit.SECONDS, 1) { "{\"event\":\"ping\"}" }
                )
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeStrings)
                    .then()
                    .thenMany(inbound.receive().asString())
                    .filter { response ->
                        !response!!.contentEquals("{\"event\":\"pong\"}")
                    }
            }
            .map { objectMapper.readValue<IdaxMessageFrame<List<IdaxOrderBook>>>(it) }
    }

    /**
     * @param symbols trade pairs for subscribe tick data
     * @param depth 5, 10, 20, 50(number of depth bars obtained)
     *
     * idax_sub_X_depth_Y  Subscription market depth (1 push per second)
     *
     */
    fun createOrderBookSnapShotFlux(
        symbols: List<CurrencyPair>,
        depth: Int
    ): Flux<IdaxMessageFrame<List<IdaxOrderBook>>> {
        val subscribeStrings = symbols.map { "${it.baseCurrency}_${it.quoteCurrency}".toLowerCase() }
            .map { "{'event':'addChannel','channel':'idax_sub_${it}_depth_$depth'}" }
            .toFlux()

        return HttpClient.create()
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeStrings)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { objectMapper.readValue<IdaxMessageFrame<List<IdaxOrderBook>>>(it) }
    }
}
