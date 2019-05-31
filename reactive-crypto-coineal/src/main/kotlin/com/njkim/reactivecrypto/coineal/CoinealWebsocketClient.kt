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

package com.njkim.reactivecrypto.coineal

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import mu.KotlinLogging
import reactor.core.publisher.Flux
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class CoinealWebsocketClient : ExchangeWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val coinealRawWebsocketClient: CoinealRawWebsocketClient =
        CoinealRawWebsocketClient()

    private val lastTickDataTimestamp = AtomicLong()
    private val tickDataTimestampDuplicateCount = AtomicInteger()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return coinealRawWebsocketClient.createTradeDataFlux(subscribeTargets)
            .flatMapIterable { coinealMessageFrame ->
                coinealMessageFrame.tick.data.map {
                    TickData(
                        "${it.id}",
                        it.ts,
                        it.price,
                        it.vol,
                        coinealMessageFrame.currencyPair,
                        ExchangeVendor.COINEAL,
                        it.side
                    )
                }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        return coinealRawWebsocketClient.createOrderBookFlux(subscribeTargets, "step0")
            .map { coinealMessageFrame ->
                OrderBook(
                    createTickDataUniqueId(coinealMessageFrame.ts.toEpochMilli()),
                    coinealMessageFrame.currencyPair,
                    coinealMessageFrame.ts,
                    ExchangeVendor.COINEAL,
                    coinealMessageFrame.tick.bids,
                    coinealMessageFrame.tick.asks
                )
            }
    }

    /**
     * add salt value for create unique Id
     */
    private fun createTickDataUniqueId(timestamp: Long): String {
        return if (lastTickDataTimestamp.getAndSet(timestamp) == timestamp) {

            val saltString = "${tickDataTimestampDuplicateCount.incrementAndGet()}".padStart(3, '0')
            "$timestamp$saltString"
        } else {
            tickDataTimestampDuplicateCount.set(0)
            val saltString = "${tickDataTimestampDuplicateCount.get()}".padStart(3, '0')
            "$timestamp$saltString"
        }
    }
}