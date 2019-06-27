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

package com.njkim.reactivecrypto.bithumb

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bithumb.model.BithumbOrderBook
import com.njkim.reactivecrypto.bithumb.model.BithumbResponseWrapper
import com.njkim.reactivecrypto.bithumb.model.BithumbTickData
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.AbstractExchangeWebsocketClient
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime

class BithumbWebsocketClient : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://wss.bithumb.com/public"

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return BithumbJsonObjectMapper()
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val subscribeRequests = subscribeTargets.stream()
            .map {
                if (it.baseCurrency == Currency.KRW) {
                    "${it.targetCurrency}"
                } else {
                    "${it.targetCurrency}${it.baseCurrency}"
                }
            }
            .map { "{\"currency\":\"$it\",\"tickDuration\":\"24H\",\"service\":\"transaction\"}" }
            .toFlux()

        return subscribeRequests.flatMap { subscribeRequest ->
            HttpClient.create()
                .headers { it.add("Origin", "https://www.bithumb.com") }
                .wiretap(log.isDebugEnabled)
                .websocket()
                .uri(baseUri)
                .handle { inbound, outbound ->
                    outbound.sendString(Flux.just(subscribeRequest))
                        .then()
                        .thenMany(inbound.receive().asString())
                }
                .map { objectMapper.readValue<BithumbResponseWrapper<List<BithumbTickData>>>(it) }
                .flatMapIterable {
                    it.data.map { bithumbTickData ->
                        TickData(
                            bithumbTickData.countNo.toString(),
                            bithumbTickData.transactionDate,
                            bithumbTickData.price,
                            bithumbTickData.unitsTraded,
                            CurrencyPair(it.header.currency, Currency.KRW), // Bithumb only have KRW market
                            ExchangeVendor.BITHUMB,
                            bithumbTickData.type
                        )
                    }
                }
        }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeRequests = subscribeTargets.stream()
            .map {
                if (it.baseCurrency == Currency.KRW) {
                    "${it.targetCurrency}"
                } else {
                    "${it.targetCurrency}${it.baseCurrency}"
                }
            }
            .map { "{\"currency\":\"$it\",\"tickDuration\":\"24H\",\"service\":\"orderbook\"}" }
            .toFlux()

        return subscribeRequests.flatMap { subscribeRequest ->
            HttpClient.create()
                .headers { it.add("Origin", "https://www.bithumb.com") }
                .wiretap(log.isDebugEnabled)
                .websocket()
                .uri(baseUri)
                .handle { inbound, outbound ->
                    outbound.sendString(Flux.just(subscribeRequest))
                        .then()
                        .thenMany(inbound.receive().asString())
                }
                .map { objectMapper.readValue<BithumbResponseWrapper<BithumbOrderBook>>(it) }
                .map {
                    OrderBook(
                        "${it.header.currency}${ZonedDateTime.now().toEpochMilli()}",
                        CurrencyPair(it.header.currency, Currency.KRW), // Bithumb only have KRW market
                        ZonedDateTime.now(),
                        ExchangeVendor.BITHUMB,
                        it.data.bids.map { bithumbBid ->
                            OrderBookUnit(
                                bithumbBid.price,
                                bithumbBid.quantity,
                                TradeSideType.BUY,
                                null
                            )
                        },
                        it.data.asks.map { bithumbAsk ->
                            OrderBookUnit(
                                bithumbAsk.price,
                                bithumbAsk.quantity,
                                TradeSideType.SELL,
                                null
                            )
                        }
                    )
                }
        }
    }
}
