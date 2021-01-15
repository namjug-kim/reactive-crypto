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

package com.njkim.reactivecrypto.core.common.util

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair

object CurrencyPairUtil {
    private val currencyPairMap: MutableMap<String, CurrencyPair> = HashMap()

    init {
        Currency.values().forEach { baseCurrency ->
            Currency.FIAT_CURRENCIES.forEach { quoteCurrency ->
                val currencyPair = CurrencyPair(baseCurrency, quoteCurrency)
                currencyPairMap["${baseCurrency.symbol}${quoteCurrency.symbol}"] = currencyPair
            }
        }
    }

    fun parse(rawValue: String): CurrencyPair? {
        val currencyPair = currencyPairMap[rawValue.toUpperCase()]
        return if (currencyPair != null) {
            currencyPair
        } else {
            Currency.FIAT_CURRENCIES
                .filter { rawValue.endsWith(it.symbol) }
                .map {
                    val targetCurrency = Currency.getInstance(rawValue.replace(it.symbol, ""))
                    CurrencyPair(targetCurrency, it)
                }
                .firstOrNull() ?: throw IllegalArgumentException("UNKNWON Currency Pair : $rawValue")
        }
    }
}
