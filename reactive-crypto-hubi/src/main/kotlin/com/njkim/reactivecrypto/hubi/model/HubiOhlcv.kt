package com.njkim.reactivecrypto.hubi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.ZonedDateTime

data class HubiOhlcv(
    @get:JsonProperty("high")
    val high: BigDecimal,

    @get:JsonProperty("low")
    val low: BigDecimal,

    @get:JsonProperty("new")
    val new: BigDecimal,

    @get:JsonProperty("open")
    val open: BigDecimal,

    @get:JsonProperty("otime")
    val otime: ZonedDateTime,

    @get:JsonProperty("rate")
    val rate: BigDecimal,
    val volume: BigDecimal
)