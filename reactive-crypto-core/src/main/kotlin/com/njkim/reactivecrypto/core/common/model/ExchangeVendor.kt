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
        @JvmField
        val UPBIT = ExchangeVendor("UPBIT")
        @JvmField
        val BINANCE = ExchangeVendor("BINANCE")
        @JvmField
        val HUOBI_GLOBAL = ExchangeVendor("HUOBI_GLOBAL")
        @JvmField
        val HUOBI_JAPAN = ExchangeVendor("HUOBI_JAPAN")
        @JvmField
        val HUOBI_KOREA = ExchangeVendor("HUOBI_KOREA")
        @JvmField
        val OKEX = ExchangeVendor("OKEX")
        @JvmField
        val OKEX_KOREA = ExchangeVendor("OKEX_KOREA")
        @JvmField
        val BITHUMB = ExchangeVendor("BITHUMB")
        @JvmField
        val HUBI = ExchangeVendor("HUBI")
        @JvmField
        val BITMEX = ExchangeVendor("BITMEX")
        @JvmField
        val KRAKEN = ExchangeVendor("KRAKEN")
        @JvmField
        val BITMAX = ExchangeVendor("BITMAX")
        @JvmField
        val IDAX = ExchangeVendor("IDAX")
        @JvmField
        val COINEAL = ExchangeVendor("COINEAL")
        @JvmField
        val POLONIEX = ExchangeVendor("POLONIEX")
        @JvmField
        val BITSTAMP = ExchangeVendor("BITSTAMP")
        @JvmField
        val KORBOTEX = ExchangeVendor("KORBOTEX")

        @JvmStatic
        fun valueOf(value: String): ExchangeVendor {
            return mapCache[value] ?: ExchangeVendor(value)
        }

        @JvmStatic
        fun values(): List<ExchangeVendor> {
            return listOf(
                UPBIT, BINANCE, HUOBI_GLOBAL, HUOBI_JAPAN, HUOBI_KOREA, OKEX, OKEX_KOREA, BITHUMB, HUBI, BITMEX,
                KRAKEN, BITMAX, IDAX, COINEAL, POLONIEX, BITSTAMP, KORBOTEX
            )
        }

        private val mapCache = values().map { it.name to it }.toMap()
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
