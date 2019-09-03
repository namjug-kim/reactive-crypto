package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class KucoinMatchExecutionData(
    @JsonProperty("makerOrderId")
    val makerOrderId: String,

    @JsonProperty("price")
    val price: BigDecimal,

    @JsonProperty("sequence")
    val sequence: BigDecimal,

    @JsonProperty("side")
    val side: TradeSideType,

    @JsonProperty("size")
    val size: BigDecimal,

    @JsonProperty("symbol")
    val symbol: CurrencyPair,

    @JsonProperty("takerOrderId")
    val takerOrderId: String,

    @JsonProperty("time")
    val time: ZonedDateTime,

    @JsonProperty("tradeId")
    val tradeId: String,

    @JsonProperty("type")
    val type: String
)