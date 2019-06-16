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

package com.njkim.reactivecrypto.bitstamp

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bitstamp.model.*
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient

/**
 * Bitstamp Websocket API v2
 * document : https://www.bitstamp.net/websocket/v2/
 */
class BitstampRawWebsocketClient {
    private val baseUrl: String = "wss://ws.bitstamp.net"

    fun liveTicker(currencyPairs: List<CurrencyPair>): Flux<BitstampMessageFrame<BitstampTradeEvent>> {
        val subscribeMessages = currencyPairs.map { createSubscribeMessage(it, BitstampEventType.TRADE) }
            .toFlux()

        return HttpClient.create()
            .websocket()
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeMessages)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { !it.contains("bts:subscription_succeeded") }
            .map { BitstampJsonObjectMapper.instance.readValue<BitstampMessageFrame<BitstampTradeEvent>>(it) }
    }

    fun liveOrderBook(currencyPairs: List<CurrencyPair>): Flux<BitstampMessageFrame<BitstampOrderBook>> {
        val subscribeMessages = currencyPairs
            .map { createSubscribeMessage(it, BitstampEventType.ORDER_BOOK) }
            .toFlux()

        return HttpClient.create()
            .websocket()
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeMessages)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { !it.contains("bts:subscription_succeeded") }
            .map { BitstampJsonObjectMapper.instance.readValue<BitstampMessageFrame<BitstampOrderBook>>(it) }
    }

    fun liveDetailOrderBook(currencyPairs: List<CurrencyPair>): Flux<BitstampMessageFrame<BitstampDetailOrderBook>> {
        val subscribeMessages = currencyPairs
            .map { createSubscribeMessage(it, BitstampEventType.DETAIL_ORDER_BOOK) }
            .toFlux()

        return HttpClient.create()
            .websocket()
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeMessages)
                    .then()
                    .thenMany(inbound.aggregateFrames().receive().asString())
            }
            .filter { !it.contains("bts:subscription_succeeded") }
            .map { BitstampJsonObjectMapper.instance.readValue<BitstampMessageFrame<BitstampDetailOrderBook>>(it) }
    }

    private fun createSubscribeMessage(currencyPair: CurrencyPair, event: BitstampEventType): String {
        val channel =
            "${event.subscribeMessage}_${currencyPair.targetCurrency}${currencyPair.baseCurrency}".toLowerCase()
        return "{" +
                "\"event\": \"bts:subscribe\"," +
                "\"data\": {" +
                "\"channel\": \"$channel\"" +
                "}" +
                "}"
    }
}
