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

import org.apache.commons.lang3.text.WordUtils

enum class ExchangeVendor {
    UPBIT,
    BINANCE,
    HUOBI_KOREA,
    OKEX,
    BITHUMB,
    HUBI,
    BITMEX,
    KRAKEN,
    BITMAX,
    IDAX,
    COINEAL;

    /**
     * format : com.njkim.reactivecrypto.$packageName.${carmelCaseName}WebsocketClient
     */
    val websocketClientName: String
        get() {
            val packageName = this.name.toLowerCase().replace("_", "")
            val carmelCaseName = WordUtils
                .capitalizeFully(this.name, '_')
                .replace("_", "")

            return "com.njkim.reactivecrypto.$packageName.${carmelCaseName}WebsocketClient"
        }

    /**
     * format : com.njkim.reactivecrypto.$packageName.${carmelCaseName}WebsocketClient
     */
    val httpClientName: String
        get() {
            val packageName = this.name.toLowerCase().replace("_", "")
            val carmelCaseName = WordUtils
                .capitalizeFully(this.name, '_')
                .replace("_", "")

            return "com.njkim.reactivecrypto.$packageName.${carmelCaseName}HttpClient"
        }
}