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

package com.njkim.reactivecrypto.core.common.model.order

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class Order(
    val uniqueId: String,
    val orderStatusType: OrderStatusType,

    val side: TradeSideType,
    val currencyPair: CurrencyPair,
    val orderPrice: BigDecimal,
    val averageTradePrice: BigDecimal?,
    val orderVolume: BigDecimal?,
    val filledVolume: BigDecimal,

    val paidFee: BigDecimal? = null,
    val reservedFee: BigDecimal? = null,
    val remainingFee: BigDecimal? = null,

    val createDateTime: ZonedDateTime

)
