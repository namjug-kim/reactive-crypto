package com.njkim.reactivecrypto.bhex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BhexTicker(
    @JsonProperty("o")
    val openPrice: BigDecimal,
    @JsonProperty("h")
    val highPrice: BigDecimal,
    @JsonProperty("l")
    val lowPrice: BigDecimal,
    @JsonProperty("c")
    val closePrice: BigDecimal,
    @JsonProperty("v")
    val volume: BigDecimal,
    @JsonProperty("s")
    val symbol: CurrencyPair,
    @JsonProperty("t")
    val time: ZonedDateTime,
    @JsonProperty("e")
    val e: String // ignore
)