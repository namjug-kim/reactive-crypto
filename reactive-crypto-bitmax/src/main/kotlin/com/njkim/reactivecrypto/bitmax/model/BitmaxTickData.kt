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

package com.njkim.reactivecrypto.bitmax.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 *
 * @see BitmaxMessageFrame message frame default property
 */
data class BitmaxTickDataWrapper(
    override val m: String,
    override val s: CurrencyPair,
    val trades: List<BitmaxTickData>
) : BitmaxMessageFrame(m, s)

/**
 * @property bm bm : if true, the buyer is the market maker
 * @property price p : price
 * @property quantity q : quantity
 * @property timestamp t : timestamp
 */
data class BitmaxTickData(
    @get:JsonProperty("bm")
    val bm: Boolean,

    @get:JsonProperty("p")
    val price: BigDecimal,

    @get:JsonProperty("q")
    val quantity: BigDecimal,

    @get:JsonProperty("t")
    val timestamp: ZonedDateTime
)