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

package com.njkim.reactivecrypto.bitstamp.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil

data class BitstampMessageFrame<T>(
    val data: T,
    val event: String,
    val channel: String
) {
    val currencyPair: CurrencyPair
        get() {
            val split = channel.split("_")
            val currencyPairRawValue = split.last()
            return CurrencyPairUtil.parse(currencyPairRawValue) ?: throw IllegalArgumentException()
        }
}
