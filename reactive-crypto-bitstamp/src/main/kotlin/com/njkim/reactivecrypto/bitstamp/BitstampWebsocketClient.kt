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

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import reactor.core.publisher.Flux

/**
 * Bitstamp Websocket API v2
 * document : https://www.bitstamp.net/websocket/v2/
 */
class BitstampWebsocketClient : ExchangeWebsocketClient {
    private val bitstampRawWebsocketClient: BitstampRawWebsocketClient = BitstampRawWebsocketClient()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return bitstampRawWebsocketClient
            .liveTicker(subscribeTargets)
            .map { bitstampMessageFrame ->
                TickData(
                    "${bitstampMessageFrame.data.id}",
                    bitstampMessageFrame.data.microTimestamp,
                    bitstampMessageFrame.data.price,
                    bitstampMessageFrame.data.amount,
                    bitstampMessageFrame.currencyPair,
                    ExchangeVendor.BITSTAMP,
                    bitstampMessageFrame.data.type
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        return bitstampRawWebsocketClient
            .liveOrderBook(subscribeTargets)
            .map { bitstampMessageFrame ->
                val microTimestamp = bitstampMessageFrame.data.microTimestamp

                OrderBook(
                    "${microTimestamp.toEpochMilli()}",
                    bitstampMessageFrame.currencyPair,
                    microTimestamp,
                    ExchangeVendor.BITSTAMP,
                    bitstampMessageFrame.data.bids.map { OrderBookUnit(it.price, it.quantity, OrderSideType.BID) },
                    bitstampMessageFrame.data.asks.map { OrderBookUnit(it.price, it.quantity, OrderSideType.ASK) }
                )
            }
    }
}
