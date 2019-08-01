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

package com.njkim.reactivecrypto.huobiglobal.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.huobiglobal.HuobiCommonUtil
import java.time.ZonedDateTime
import java.util.regex.Pattern

data class HuobiSubscribeResponse<T>(
    val ch: String,
    val ts: ZonedDateTime,
    val tick: T
) {
    val currencyPair: CurrencyPair
        get() {
            val pattern = Pattern.compile("(market.)([a-z0-9]+)(.*)", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(ch)
            if (matcher.matches()) {
                val group = matcher.group(2)
                return HuobiCommonUtil.parseCurrencyPair(group)
            }

            throw IllegalArgumentException()
        }
}
