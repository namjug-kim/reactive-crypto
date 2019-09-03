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

package com.njkim.reactivecrypto.core.common.model

import com.njkim.reactivecrypto.core.common.util.toCarmelCase

data class ExchangeVendor(val name: String) {

    companion object {
        private val mapCache: MutableMap<String, ExchangeVendor> = HashMap()

        @JvmField
        val UPBIT = getInstance("UPBIT")
        @JvmField
        val BINANCE = getInstance("BINANCE")
        @JvmField
        val HUOBI_GLOBAL = getInstance("HUOBI_GLOBAL")
        @JvmField
        val HUOBI_JAPAN = getInstance("HUOBI_JAPAN")
        @JvmField
        val HUOBI_KOREA = getInstance("HUOBI_KOREA")
        @JvmField
        val OKEX = getInstance("OKEX")
        @JvmField
        val OKEX_KOREA = getInstance("OKEX_KOREA")
        @JvmField
        val BITHUMB = getInstance("BITHUMB")
        @JvmField
        val HUBI = getInstance("HUBI")
        @JvmField
        val BITMEX = getInstance("BITMEX")
        @JvmField
        val KRAKEN = getInstance("KRAKEN")
        @JvmField
        val BITMAX = getInstance("BITMAX")
        @JvmField
        val IDAX = getInstance("IDAX")
        @JvmField
        val COINEAL = getInstance("COINEAL")
        @JvmField
        val POLONIEX = getInstance("POLONIEX")
        @JvmField
        val BITSTAMP = getInstance("BITSTAMP")
        @JvmField
        val KORBOTEX = getInstance("KORBOTEX")
        @JvmField
        val COINALL = getInstance("COINALL")
        @JvmField
        val BHEX = getInstance("BHEX")
        @JvmField
        val BITZ = getInstance("BITZ")
        @JvmField
        val KUCOIN = getInstance("KUCOIN")

        @JvmStatic
        fun getInstance(value: String): ExchangeVendor {
            return mapCache.computeIfAbsent(value) { ExchangeVendor(value) }
        }

        @JvmStatic
        fun values(): Collection<ExchangeVendor> {
            return mapCache.values
        }
    }

    /**
     * format : com.njkim.reactivecrypto.$packageName.${carmelCaseName}WebsocketClient
     */
    val websocketClientName: String
        get() {
            val packageName = this.name.toLowerCase().replace("_", "")
            val className = this.name.toCarmelCase()
                .capitalize()

            return "com.njkim.reactivecrypto.$packageName.${className}WebsocketClient"
        }

    /**
     * format : com.njkim.reactivecrypto.$packageName.${carmelCaseName}WebsocketClient
     */
    val httpClientName: String
        get() {
            val packageName = this.name.toLowerCase().replace("_", "")
            val className = this.name.toCarmelCase()
                .capitalize()

            return "com.njkim.reactivecrypto.$packageName.http.${className}HttpClient"
        }

    override fun toString(): String {
        return name
    }
}
