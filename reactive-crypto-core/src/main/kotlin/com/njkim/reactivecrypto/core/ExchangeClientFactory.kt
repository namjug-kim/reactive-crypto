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
import com.njkim.reactivecrypto.core.websocket.ExchangePrivateWebsocketClient
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.primaryConstructor

object ExchangeClientFactory {
    init {
        ExchangeVendor.values()
            .forEach { exchangeVendor ->
                ReactiveCryptoPlugins.customClientFactory
                    .addHttpCustomFactory(exchangeVendor, defaultHttpFactory(exchangeVendor))
                ReactiveCryptoPlugins.customClientFactory
                    .addPublicWsCustomFactory(exchangeVendor, defaultPublicWsFactory(exchangeVendor))
                ReactiveCryptoPlugins.customClientFactory
                    .addPrivateWsCustomFactory(exchangeVendor, defaultPrivateWsFactory(exchangeVendor))
            }
    }

    @JvmStatic
    fun publicWebsocket(exchangeVendor: ExchangeVendor): ExchangePublicWebsocketClient {
        return ReactiveCryptoPlugins.customClientFactory.getCustomPublicWsFactory(exchangeVendor)
            ?.let { it() }
            ?: defaultPublicWsFactory(exchangeVendor)()
    }

    @JvmStatic
    fun privateWebsocket(exchangeVendor: ExchangeVendor, accessKey: String, secretKey: String): ExchangePrivateWebsocketClient {
        return ReactiveCryptoPlugins.customClientFactory.getCustomPrivateWsFactory(exchangeVendor)
            ?.let { it(accessKey, secretKey) }
            ?: defaultPrivateWsFactory(exchangeVendor)(accessKey, secretKey)
    }

    @JvmStatic
    fun http(exchangeVendor: ExchangeVendor): ExchangeHttpClient {
        return ReactiveCryptoPlugins.customClientFactory.getCustomHttpFactory(exchangeVendor)
            ?.let { it() }
            ?: defaultHttpFactory(exchangeVendor)()
    }

    private fun defaultPublicWsFactory(exchangeVendor: ExchangeVendor): PublicFactoryFunction<ExchangePublicWebsocketClient> {
        return {
            val websocketClientClass = Class.forName(exchangeVendor.publicWebsocketClientName).kotlin
            websocketClientClass.createInstance() as ExchangePublicWebsocketClient
        }
    }

    private fun defaultPrivateWsFactory(exchangeVendor: ExchangeVendor): PrivateFactoryFunction<ExchangePrivateWebsocketClient> {
        return { accessKey: String, secretKey: String ->
            val websocketClientClass = Class.forName(exchangeVendor.privateWebsocketClientName).kotlin
            val primaryConstructor = websocketClientClass.primaryConstructor ?: error("primary construct empty")
            primaryConstructor.call(accessKey, secretKey) as ExchangePrivateWebsocketClient
        }
    }

    private fun defaultHttpFactory(exchangeVendor: ExchangeVendor): PublicFactoryFunction<ExchangeHttpClient> {
        return {
            val httpClientClass = Class.forName(exchangeVendor.httpClientName).kotlin
            httpClientClass.createInstance() as ExchangeHttpClient
        }
    }
}

typealias PublicFactoryFunction<T> = () -> T
typealias PrivateFactoryFunction<T> = (accessKey: String, secretKey: String) -> T
