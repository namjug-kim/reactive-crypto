package com.njkim.reactivecrypto.hubi.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class HubiOrderBook(
    @get:JsonProperty("asks")
    val asks: List<HubiOrderBookUnit>,

    @get:JsonProperty("bids")
    val bids: List<HubiOrderBookUnit>
)

data class HubiOrderBookUnit(
    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("amount")
    val amount: BigDecimal
)