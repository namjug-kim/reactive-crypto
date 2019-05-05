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

package com.njkim.reactivecrypto.okex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * {
 * ”table”: "spot/trade”,
 * ”data ”: [
 * [
 * {
 * ”instrument_id”: "ETH-USDT”,
 * ”price”: "22888”,
 * ”side”: "buy”,
 * ”size”: "7”,
 * ”timestamp”: "2018-11-22T03:58:57.709Z”,
 * ”trade_id”: "108223090144493569”
 * }]
 * ]
 * }
 */
data class OkexTickDataWrapper(
    @get:JsonProperty("table")
    val table: String,

    @get:JsonProperty("data")
    val data: List<OkexTickData>
)

data class OkexTickData(
    @get:JsonProperty("instrument_id")
    val instrumentId: CurrencyPair,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("side")
    val side: TradeSideType,

    @get:JsonProperty("size")
    val size: BigDecimal,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("trade_id")
    val tradeId: String
)