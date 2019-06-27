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

package com.njkim.reactivecrypto.coineal.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.OrderStatusType
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class CoinealOpenOrder(
    @JsonProperty("side")
    val side: OrderSideType,
    @JsonProperty("total_price")
    val totalPrice: BigDecimal,
    @JsonProperty("created_at")
    val createdAt: ZonedDateTime,
    @JsonProperty("avg_price")
    val avgPrice: BigDecimal,
    @JsonProperty("countCoin")
    val countCoin: String,
    @JsonProperty("source")
    val source: Int,
    @JsonProperty("type")
    val type: OrderType,
    @JsonProperty("side_msg")
    val sideMsg: String,
    @JsonProperty("volume")
    val volume: BigDecimal,
    @JsonProperty("price")
    val price: BigDecimal,
    @JsonProperty("source_msg")
    val sourceMsg: String,
    @JsonProperty("status_msg")
    val statusMsg: String,
    @JsonProperty("deal_volume")
    val dealVolume: BigDecimal,
    @JsonProperty("id")
    val id: Int,
    @JsonProperty("remain_volume")
    val remainVolume: String,
    @JsonProperty("baseCoin")
    val baseCoin: Currency,
    @JsonProperty("tradeList")
    val tradeList: List<CoinealTradeResult>,
    @JsonProperty("status")
    val status: OrderStatusType
)
