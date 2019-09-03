package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal

data class KucoinMarketLevel2(
    @JsonProperty("changes")
    val changes: Changes,
    @JsonProperty("sequenceEnd")
    val sequenceEnd: BigDecimal,
    @JsonProperty("sequenceStart")
    val sequenceStart: BigDecimal,
    @JsonProperty("symbol")
    val symbol: CurrencyPair
) {
    data class Changes(
        @JsonProperty("asks")
        val asks: List<KucoinOrderBookUnit>,
        @JsonProperty("bids")
        val bids: List<KucoinOrderBookUnit>
    )
}
