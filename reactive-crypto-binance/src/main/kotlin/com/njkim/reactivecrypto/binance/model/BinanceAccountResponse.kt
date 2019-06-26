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
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BinanceAccountResponse(
    @JsonProperty("makerCommission")
    val makerCommission: Int,
    @JsonProperty("takerCommission")
    val takerCommission: Int,
    @JsonProperty("buyerCommission")
    val buyerCommission: Int,
    @JsonProperty("sellerCommission")
    val sellerCommission: Int,
    @JsonProperty("canTrade")
    val canTrade: Boolean,
    @JsonProperty("canWithdraw")
    val canWithdraw: Boolean,
    @JsonProperty("canDeposit")
    val canDeposit: Boolean,
    @JsonProperty("updateTime")
    val updateTime: ZonedDateTime,
    @JsonProperty("balances")
    val balances: List<Balance>
) {
    data class Balance(
        @JsonProperty("asset")
        val asset: Currency,
        @JsonProperty("free")
        val free: BigDecimal,
        @JsonProperty("locked")
        val locked: BigDecimal
    )
}
