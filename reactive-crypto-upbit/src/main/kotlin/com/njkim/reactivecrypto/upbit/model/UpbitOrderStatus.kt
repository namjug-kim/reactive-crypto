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
import com.njkim.reactivecrypto.core.common.model.order.OrderStatus
import com.njkim.reactivecrypto.core.common.model.order.OrderStatusType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * @author traeper
 */
data class UpbitOrderStatus(

    @get:JsonProperty("uuid")
    val uuid: String,

    @get:JsonProperty("side")
    val side: OrderSideType,

    @get:JsonProperty("ord_type")
    val ordType: String,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("state")
    val orderStatusType: OrderStatusType,

    @get:JsonProperty("market")
    val currencyPair: CurrencyPair,

    @get:JsonProperty("created_at")
    val createdAt: ZonedDateTime,

    @get:JsonProperty("volume")
    val volume: BigDecimal,

    @get:JsonProperty("remaining_volume")
    val remainingVolume: BigDecimal,

    @get:JsonProperty("reserved_fee")
    val reservedFee: BigDecimal,

    @get:JsonProperty("remaining_fee")
    val remainingFee: BigDecimal,

    @get:JsonProperty("paid_fee")
    val paidFee: BigDecimal,

    @get:JsonProperty("locked")
    val locked: BigDecimal,

    @get:JsonProperty("executed_volume")
    val executedVolume: BigDecimal,

    @get:JsonProperty("trades_count")
    val tradesCount: Int
)
