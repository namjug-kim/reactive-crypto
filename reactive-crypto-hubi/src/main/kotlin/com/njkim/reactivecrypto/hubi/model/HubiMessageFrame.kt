package com.njkim.reactivecrypto.hubi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.time.ZonedDateTime

data class HubiMessageFrame<T>(
    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime?,

    @get:JsonProperty("symbol")
    val symbol: CurrencyPair,

    @get:JsonProperty("dataType")
    val dataType: String,

    @get:JsonProperty("data")
    val data: T
)