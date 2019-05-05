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

package com.njkim.reactivecrypto.binance

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.binance.model.BinanceOrderBook
import com.njkim.reactivecrypto.binance.model.BinanceResponseWrapper
import com.njkim.reactivecrypto.binance.model.BinanceTickData
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.stream.Collectors

class BinanceWebsocketClient : ExchangeWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://stream.binance.com:9443"
    private val objectMapper: ObjectMapper = BinanceJsonObjectMapper.instance

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val streams = subscribeTargets.stream()
            .map { "${it.targetCurrency}${it.baseCurrency}" }
            .map { it.toLowerCase() + "@trade" }
            .collect(Collectors.joining("/"))

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri("$baseUri/stream?streams=$streams")
            .handle { inbound, _ -> inbound.receive().asString() }
            .map { objectMapper.readValue<BinanceResponseWrapper<BinanceTickData>>(it) }
            .map { it.data }
            .map { binanceTradeRawData ->
                TickData(
                    binanceTradeRawData.tradeId.toString() + binanceTradeRawData.currencyPair + binanceTradeRawData.eventTime.toEpochMilli(),
                    binanceTradeRawData.eventTime,
                    binanceTradeRawData.price,
                    binanceTradeRawData.quantity,
                    binanceTradeRawData.currencyPair,
                    ExchangeVendor.BINANCE
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val streams = subscribeTargets.stream()
            .map { "${it.targetCurrency}${it.baseCurrency}" }
            .map { it.toLowerCase() + "@depth20" }
            .collect(Collectors.joining("/"))

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri("$baseUri/stream?streams=$streams")
            .handle { inbound, _ -> inbound.receive().asString() }
            .map { objectMapper.readValue<BinanceResponseWrapper<BinanceOrderBook>>(it) }
            .map {
                OrderBook(
                    "${it.data.lastUpdateId}",
                    it.getCurrencyPair(),
                    ZonedDateTime.now(),
                    ExchangeVendor.BINANCE,
                    it.data.getBids(),
                    it.data.getAsks()
                )
            }

    }
}