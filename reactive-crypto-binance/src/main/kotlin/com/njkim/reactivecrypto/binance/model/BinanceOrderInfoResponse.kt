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

package com.njkim.reactivecrypto.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import com.njkim.reactivecrypto.core.common.model.order.TimeInForceType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BinanceOrderInfoResponse(
    @JsonProperty("symbol")
    val symbol: CurrencyPair,
    @JsonProperty("orderId")
    val orderId: Long,
    @JsonProperty("clientOrderId")
    val clientOrderId: String,
    @JsonProperty("price")
    val price: BigDecimal,
    @JsonProperty("origQty")
    val origQty: BigDecimal,
    @JsonProperty("executedQty")
    val executedQty: BigDecimal,
    @JsonProperty("cummulativeQuoteQty")
    val cummulativeQuoteQty: BigDecimal,
    @JsonProperty("status")
    val status: BinanceOrderStatusType,
    @JsonProperty("timeInForce")
    val timeInForce: TimeInForceType,
    @JsonProperty("type")
    val type: OrderType,
    @JsonProperty("side")
    val side: TradeSideType,
    @JsonProperty("stopPrice")
    val stopPrice: BigDecimal,
    @JsonProperty("icebergQty")
    val icebergQty: BigDecimal,
    @JsonProperty("time")
    val time: ZonedDateTime,
    @JsonProperty("updateTime")
    val updateTime: ZonedDateTime,
    @JsonProperty("isWorking")
    val isWorking: Boolean
)
