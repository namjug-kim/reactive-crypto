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
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient

class CustomClientFactory {
    private val customWsFactory: MutableMap<ExchangeVendor, FactoryFunction<ExchangeWebsocketClient>> = hashMapOf()
    private val customHttpFactory: MutableMap<ExchangeVendor, FactoryFunction<ExchangeHttpClient>> = hashMapOf()

    fun addWsCustomFactory(exchangeVendor: ExchangeVendor, factory: FactoryFunction<ExchangeWebsocketClient>) {
        customWsFactory[exchangeVendor] = factory
    }

    fun addHttpCustomFactory(exchangeVendor: ExchangeVendor, factory: FactoryFunction<ExchangeHttpClient>) {
        customHttpFactory[exchangeVendor] = factory
    }

    fun getCustomHttpFactory(exchangeVendor: ExchangeVendor): FactoryFunction<ExchangeHttpClient>? {
        return this.customHttpFactory[exchangeVendor]
    }

    fun getCustomWsFactory(exchangeVendor: ExchangeVendor): FactoryFunction<ExchangeWebsocketClient>? {
        return this.customWsFactory[exchangeVendor]
    }
}

typealias FactoryFunction<T> = (ExchangeVendor) -> T
