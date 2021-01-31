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
import com.njkim.reactivecrypto.core.http.ExchangeHttpClient
import com.njkim.reactivecrypto.core.websocket.ExchangePrivateWebsocketClient
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient

class CustomClientFactory {
    private val customPublicWsFactory: MutableMap<ExchangeVendor, PublicFactoryFunction<ExchangePublicWebsocketClient>> = hashMapOf()
    private val customPrivateWsFactory: MutableMap<ExchangeVendor, PrivateFactoryFunction<ExchangePrivateWebsocketClient>> = hashMapOf()
    private val customHttpFactory: MutableMap<ExchangeVendor, PublicFactoryFunction<ExchangeHttpClient>> = hashMapOf()

    fun addPublicWsCustomFactory(exchangeVendor: ExchangeVendor, factory: PublicFactoryFunction<ExchangePublicWebsocketClient>) {
        customPublicWsFactory[exchangeVendor] = factory
    }

    fun addPrivateWsCustomFactory(exchangeVendor: ExchangeVendor, factory: PrivateFactoryFunction<ExchangePrivateWebsocketClient>) {
        customPrivateWsFactory[exchangeVendor] = factory
    }

    fun addHttpCustomFactory(exchangeVendor: ExchangeVendor, factory: PublicFactoryFunction<ExchangeHttpClient>) {
        customHttpFactory[exchangeVendor] = factory
    }

    fun getCustomHttpFactory(exchangeVendor: ExchangeVendor): PublicFactoryFunction<ExchangeHttpClient>? {
        return this.customHttpFactory[exchangeVendor]
    }

    fun getCustomPublicWsFactory(exchangeVendor: ExchangeVendor): PublicFactoryFunction<ExchangePublicWebsocketClient>? {
        return this.customPublicWsFactory[exchangeVendor]
    }

    fun getCustomPrivateWsFactory(exchangeVendor: ExchangeVendor): PrivateFactoryFunction<ExchangePrivateWebsocketClient>? {
        return this.customPrivateWsFactory[exchangeVendor]
    }
}

typealias PublicFactoryFunction<T> = () -> T
typealias PrivateFactoryFunction<T> = (accessKey: String, secretKey: String) -> T
