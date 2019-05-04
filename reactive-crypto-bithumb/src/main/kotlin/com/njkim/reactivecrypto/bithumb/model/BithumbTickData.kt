package com.njkim.reactivecrypto.bithumb.model

import com.fasterxml.jackson.annotation.JsonProperty
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
    val type: String,

    @get:JsonProperty("units_traded")
    val unitsTraded: BigDecimal
)