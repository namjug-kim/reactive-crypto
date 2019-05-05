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

package com.njkim.reactivecrypto.bitmex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BitmexTickData(
    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("symbol")
    val symbol: CurrencyPair,

    @get:JsonProperty("side")
    val side: TradeSideType,

    @get:JsonProperty("size")
    val size: BigDecimal,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("tickDirection")
    val tickDirection: String,

    @get:JsonProperty("trdMatchID")
    val trdMatchID: String,

    @get:JsonProperty("grossValue")
    val grossValue: BigDecimal,

    @get:JsonProperty("homeNotional")
    val homeNotional: BigDecimal,

    @get:JsonProperty("foreignNotional")
    val foreignNotional: BigDecimal
)