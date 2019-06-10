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
        @JvmStatic
        fun websocket(exchangeVendor: ExchangeVendor): ExchangeWebsocketClient {
            return websocket(exchangeVendor.websocketClientName)
        }

        @JvmStatic
        fun websocket(exchangeVendor: String): ExchangeWebsocketClient {
            val customFactory: FactoryFunction<ExchangeWebsocketClient>? =
                ReactiveCryptoPlugins.customClientFactory.customWsFactory()[exchangeVendor]

            return if (customFactory != null) {
                customFactory(exchangeVendor)
            } else {
                val websocketClientClass = Class.forName(exchangeVendor)?.kotlin
                websocketClientClass?.createInstance() as ExchangeWebsocketClient
            }
        }

        @JvmStatic
        fun http(exchangeVendor: ExchangeVendor): ExchangeHttpClient {
            return http(exchangeVendor.httpClientName)
        }

        @JvmStatic
        fun http(exchangeVendor: String): ExchangeHttpClient {
            val customFactory: FactoryFunction<ExchangeHttpClient>? =
                ReactiveCryptoPlugins.customClientFactory.customHttpFactory()[exchangeVendor]

            return if (customFactory != null) {
                customFactory(exchangeVendor)
            } else {
                val httpClientClass = Class.forName(exchangeVendor)?.kotlin
                return httpClientClass?.createInstance() as ExchangeHttpClient
            }
        }
    }
}
