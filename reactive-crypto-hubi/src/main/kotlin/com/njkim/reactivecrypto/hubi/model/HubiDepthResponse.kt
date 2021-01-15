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

package com.njkim.reactivecrypto.hubi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class HubiDepthResponse(
    @get:JsonProperty("buyDepth")
    val buyDepth: List<HubiDepthUnit>,

    @get:JsonProperty("sellDepth")
    val sellDepth: List<HubiDepthUnit>,

    @get:JsonProperty("trades")
    val trades: List<HubiTradeUnit>,

    @get:JsonProperty("key")
    val key: CurrencyPair,

    @get:JsonProperty("event")
    val event: String
)

data class HubiDepthUnit(
    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("qty")
    val qty: BigDecimal,

    @get:JsonProperty("count")
    val count: Int,

    @get:JsonProperty("iceCount")
    val iceCount: Int
)

data class HubiTradeUnit(
    @get:JsonProperty("id")
    val id: String,

    @get:JsonProperty("symbol")
    val symbol: CurrencyPair,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("qty")
    val qty: BigDecimal,

    @get:JsonProperty("buyActive")
    val buyActive: Boolean,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime
)
