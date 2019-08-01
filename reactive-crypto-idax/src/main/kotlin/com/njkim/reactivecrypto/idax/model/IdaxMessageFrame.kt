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

package com.njkim.reactivecrypto.idax.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.idax.IdaxJsonObjectMapper
import java.util.regex.Pattern

/**
 *
 * @property channel IDAX provided data type (idax_sub_X_Y)
 * @property code "00000": register/unregister success, others register/unregister failure
 */
data class IdaxMessageFrame<T>(
    val channel: String,
    val code: String,
    val data: T
) {
    val currencyPair: CurrencyPair
        get() {
            val pattern = Pattern.compile("(idax_sub_)([a-z0-9]+_[a-z0-9]+)(_.*)", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(channel)
            if (matcher.matches()) {
                val group = matcher.group(2)
                return IdaxJsonObjectMapper.instance.convertValue(group, CurrencyPair::class.java)
            }

            throw IllegalArgumentException()
        }
}