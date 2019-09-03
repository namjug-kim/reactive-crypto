package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.ZonedDateTime

data class KucoinOrderBookSnapshotResponse(
    @JsonProperty("asks")
    val asks: List<KucoinOrderBookUnit>,
    @JsonProperty("bids")
    val bids: List<KucoinOrderBookUnit>,
    @JsonProperty("sequence")
    val sequence: BigDecimal,
    @JsonProperty("time")
    val time: ZonedDateTime
)
