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

package com.njkim.reactivecrypto.core

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.http.ExchangeHttpClient
import com.njkim.reactivecrypto.core.plugin.ReactiveCryptoPlugins
import com.njkim.reactivecrypto.core.plugin.strategy.FactoryFunction
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import kotlin.reflect.full.createInstance

class ExchangeClientFactory {
    companion object {
        init {
            ExchangeVendor.values()
                .forEach { exchangeVendor ->
                    ReactiveCryptoPlugins.customClientFactory
                        .addHttpCustomFactory(exchangeVendor) { defaultHttpFactory(exchangeVendor) }
                    ReactiveCryptoPlugins.customClientFactory
                        .addWsCustomFactory(exchangeVendor) { defaultWsFactory(exchangeVendor) }
                }
        }

        @JvmStatic
        fun websocket(exchangeVendor: ExchangeVendor): ExchangeWebsocketClient {
            val customFactory: FactoryFunction<ExchangeWebsocketClient>? =
                ReactiveCryptoPlugins.customClientFactory.getCustomWsFactory(exchangeVendor)

            return if (customFactory != null) {
                customFactory(exchangeVendor)
            } else {
                defaultWsFactory(exchangeVendor)
            }
        }

        private fun defaultWsFactory(exchangeVendor: ExchangeVendor): ExchangeWebsocketClient {
            val websocketClientClass = Class.forName(exchangeVendor.websocketClientName)?.kotlin
            return websocketClientClass?.createInstance() as ExchangeWebsocketClient
        }

        @JvmStatic
        fun http(exchangeVendor: ExchangeVendor): ExchangeHttpClient {
            val customFactory: FactoryFunction<ExchangeHttpClient>? =
                ReactiveCryptoPlugins.customClientFactory.getCustomHttpFactory(exchangeVendor)

            return if (customFactory != null) {
                customFactory(exchangeVendor)
            } else {
                defaultHttpFactory(exchangeVendor)
            }
        }

        private fun defaultHttpFactory(exchangeVendor: ExchangeVendor): ExchangeHttpClient {
            val httpClientClass = Class.forName(exchangeVendor.httpClientName)?.kotlin
            return httpClientClass?.createInstance() as ExchangeHttpClient
        }
    }
}
