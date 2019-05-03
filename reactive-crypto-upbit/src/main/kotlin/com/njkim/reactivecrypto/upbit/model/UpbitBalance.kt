package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import java.math.BigDecimal

data class UpbitBalance(
    @get:JsonProperty("currency")
    val currency: Currency,

    @get:JsonProperty("balance")
    val balance: BigDecimal,

    @get:JsonProperty("locked")
    val locked: BigDecimal,

    @get:JsonProperty("avg_buy_price")
    val avgBuyPrice: BigDecimal,

    @get:JsonProperty("avg_buy_price_modified")
    val avgBuyPriceModified: Boolean,

    @get:JsonProperty("unit_currency")
    val unitCurrency: String,

    @get:JsonProperty("avg_krw_buy_price")
    val avgKrwBuyPrice: BigDecimal,

    @get:JsonProperty("modified")
    val modified: Boolean
)