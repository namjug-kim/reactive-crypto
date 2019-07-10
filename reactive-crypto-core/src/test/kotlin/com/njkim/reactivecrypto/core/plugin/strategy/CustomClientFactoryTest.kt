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
package com.njkim.reactivecrypto.core.plugin.strategy

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.core.publisher.Flux

class CustomClientFactoryTest {
    @Test
    fun `add custom factory`() {
        // GIVEN
        val customExchangeWebsocketClient = object : ExchangeWebsocketClient {
            override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
                return Flux.empty()
            }

            override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
                return Flux.empty()
            }
        }

        val customClientFactory = CustomClientFactory()
        val testExchangeVendor = ExchangeVendor("TEST-EXCHANGE")
        customClientFactory.addWsCustomFactory(testExchangeVendor) { _ ->
            customExchangeWebsocketClient
        }

        // WHEN
        val factoryFunction = customClientFactory.getCustomWsFactory(testExchangeVendor)

        // THEN
        assertThat(factoryFunction).isNotNull
        assertThat(factoryFunction!!(testExchangeVendor)).isEqualTo(customExchangeWebsocketClient)
    }
}
