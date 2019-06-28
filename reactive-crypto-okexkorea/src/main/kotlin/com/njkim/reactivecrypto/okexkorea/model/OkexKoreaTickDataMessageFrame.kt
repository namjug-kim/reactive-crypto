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

package com.njkim.reactivecrypto.okexkorea.model

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.util.regex.Pattern

data class OkexKoreaTickDataMessageFrame(
    val channel: String,
    val binary: Int,
    val data: List<OkexKoreaTickData>
) {
    val currencyPair: CurrencyPair
        get() {
            // channel format : ok_sub_spot_{base currency}_{quote currency}_deals
            val pattern = Pattern.compile("ok_sub_spot_([a-z]+)_([a-z]+)_deals", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(channel)
            if (matcher.matches()) {
                val targetCurrency = Currency.valueOf(matcher.group(1).toUpperCase())
                val baseCurrency = Currency.valueOf(matcher.group(2).toUpperCase())

                return CurrencyPair(targetCurrency, baseCurrency)
            }

            throw IllegalArgumentException()
        }
}
