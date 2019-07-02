package com.njkim.reactivecrypto.coineal.model

/**
Created by jay on 02/07/2019
 **/
import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import java.math.BigDecimal

class CoinealBalance(
    @get:JsonProperty("coin")
    val currency: Currency,

    @get:JsonProperty("normal")
    val normal: BigDecimal,

    @get:JsonProperty("locked")
    val locked: BigDecimal,

    @get:JsonProperty("btcValuatin")
    val btcValuatIn: BigDecimal
)