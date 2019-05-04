package com.njkim.reactivecrypto.bithumb.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class BithumbOrderBook(
    @get:JsonProperty("asks")
    val asks: List<BithumbOrderBookUnit>,

    @get:JsonProperty("bids")
    val bids: List<BithumbOrderBookUnit>
)

data class BithumbOrderBookUnit(
    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("quantity")
    val quantity: BigDecimal
)