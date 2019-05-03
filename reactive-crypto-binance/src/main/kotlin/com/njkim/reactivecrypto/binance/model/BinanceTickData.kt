package com.njkim.reactivecrypto.binance.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BinanceTickData(
    @JsonProperty("e")
    val eventType: String,

    @JsonProperty("E")
    val eventTime: ZonedDateTime,

    @JsonProperty("s")
    val currencyPair: CurrencyPair,

    @JsonProperty("t")
    val tradeId: Long,

    @JsonProperty("p")
    val price: BigDecimal,

    @JsonProperty("q")
    val quantity: BigDecimal,

    @JsonProperty("b")
    val buyerOrderId: Long,

    @JsonProperty("a")
    val sellerOrderId: Long,

    @JsonProperty("m")
    val isMarketMaker: Boolean
)