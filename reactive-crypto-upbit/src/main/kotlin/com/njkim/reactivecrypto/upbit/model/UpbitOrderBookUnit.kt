package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class UpbitOrderBookUnit(
    @get:JsonProperty("ask_price")
    val askPrice: BigDecimal,

    @get:JsonProperty("bid_price")
    val bidPrice: BigDecimal,

    @get:JsonProperty("ask_size")
    val askSize: BigDecimal,

    @get:JsonProperty("bid_size")
    val bidSize: BigDecimal
)