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

package com.njkim.reactivecrypto.korbotex

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.okex.OkexWebsocketClient
import reactor.core.publisher.Flux

class KorbotexWebsocketClient : OkexWebsocketClient("wss://okexcomreal.bafang.com:8443/ws/v3?brokerId=151") {
    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        return super.createDepthSnapshot(subscribeTargets)
            .map { it.copy(exchangeVendor = ExchangeVendor.KORBOTEX) }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return super.createTradeWebsocket(subscribeTargets)
            .map { it.copy(exchangeVendor = ExchangeVendor.KORBOTEX) }
    }
}
