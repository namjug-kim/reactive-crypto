package com.njkim.reactivecrypto.bhex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.time.ZonedDateTime

data class BhexOrderBook(
    @JsonProperty("t")
    val eventTime: ZonedDateTime,

    @JsonProperty("s")
    val symbol: CurrencyPair,

    @JsonProperty("b")
    val bids: List<BhexOrderBookUnit>,

    @JsonProperty("a")
    val asks: List<BhexOrderBookUnit>
)