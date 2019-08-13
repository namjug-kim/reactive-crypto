package com.njkim.reactivecrypto.bitz.model

import com.fasterxml.jackson.annotation.JsonProperty

data class BitzOrderBook(
    @JsonProperty("bids")
    private val nullableBids: List<BitzOrderBookUnit>?,

    @JsonProperty("asks")
    private val nullableAsks: List<BitzOrderBookUnit>?
) {
    val bids: List<BitzOrderBookUnit>
        get() {
            return nullableBids ?: emptyList()
        }

    val asks: List<BitzOrderBookUnit>
        get() {
            return nullableAsks ?: emptyList()
        }
}