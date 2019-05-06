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

package com.njkim.reactivecrypto.bithumb.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * {
 * "cont_no":34601963,
 * "price":"6632000",
 * "total":"952355.2",
 * "transaction_date":"2019-05-04 22:05:49.530989",
 * "type":"up",
 * "units_traded":"0.1436"
 * }
 *
 * @property type   dn : sell, up : buy
 */
data class BithumbTickData(
    @get:JsonProperty("count_no")
    val countNo: Long,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("total")
    val total: BigDecimal,

    @get:JsonProperty("transaction_date")
    val transactionDate: ZonedDateTime,

    @get:JsonProperty("type")
    val type: TradeSideType,

    @get:JsonProperty("units_traded")
    val unitsTraded: BigDecimal
)