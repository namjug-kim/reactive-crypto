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

package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class UpbitTickData(
    @get:JsonProperty("type")
    val type: String,

    @get:JsonProperty("code")
    val code: CurrencyPair,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("trade_date")
    val tradeDate: String,

    @get:JsonProperty("trade_time")
    val tradeTime: String,

    @get:JsonProperty("trade_timestamp")
    val tradeTimestamp: ZonedDateTime,

    @get:JsonProperty("trade_price")
    val tradePrice: BigDecimal,

    @get:JsonProperty("trade_volume")
    val tradeVolume: BigDecimal,

    @get:JsonProperty("ask_bid")
    val askBid: OrderSideType,

    @get:JsonProperty("prev_closing_price")
    val prevClosingPrice: BigDecimal,

    @get:JsonProperty("change")
    val change: String,

    @get:JsonProperty("change_price")
    val changePrice: BigDecimal,

    @get:JsonProperty("sequential_id")
    val sequentialId: Long,

    @get:JsonProperty("stream_type")
    val streamType: String
)