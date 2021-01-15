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

package com.njkim.reactivecrypto.bitmax

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bitmax.model.BitmaxOrderBookWrapper
import com.njkim.reactivecrypto.bitmax.model.BitmaxTickDataWrapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient

class BitmaxRawWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://bitmax.io"

    companion object {
        val objectMapper: ObjectMapper = BitmaxJsonObjectMapper().objectMapper()
    }

    /**
     * @param symbol must be seperated by a hyphen(-). ex) ETH-BTC
     * @param recentTradeMaxCount max number of recent trades to be included in the first market trades message
     */
    fun createTradeDataFlux(symbol: CurrencyPair, recentTradeMaxCount: Int): Flux<BitmaxTickDataWrapper> {
        val targetUri: String = "$baseUri/api/public/${symbol.baseCurrency}-${symbol.quoteCurrency}"
        val subscribeMessage: String =
            "{\"messageType\":\"subscribe\",\"marketDepthLevel\":0,\"recentTradeMaxCount\": $recentTradeMaxCount,\"skipSummary\":true,\"skipBars\":true}"

        return HttpClient.create()
            .websocket()
            .uri(targetUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { it.contains("\"m\":\"marketTrades\"") }
            .map { objectMapper.readValue<BitmaxTickDataWrapper>(it) }
    }

    /**
     * @param symbol must be seperated by a hyphen(-). ex) ETH-BTC
     * @param marketDepthLevel max number of price levels on each side to be included in the first market depth message
     */
    fun createOrderBookFlux(symbol: CurrencyPair, marketDepthLevel: Int): Flux<BitmaxOrderBookWrapper> {
        val targetUri: String = "$baseUri/api/public/${symbol.baseCurrency}-${symbol.quoteCurrency}"
        val subscribeMessage: String =
            "{\"messageType\":\"subscribe\",\"marketDepthLevel\":$marketDepthLevel,\"recentTradeMaxCount\": 0,\"skipSummary\":true,\"skipBars\":true}"

        return HttpClient.create()
            .websocket()
            .uri(targetUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { it.contains("\"m\":\"depth\"") }
            .map { objectMapper.readValue<BitmaxOrderBookWrapper>(it) }
    }
}
