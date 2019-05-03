package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class UpbitOrderBook(
    @get:JsonProperty("type")
    val type: String,

    @get:JsonProperty("code")
    val code: CurrencyPair,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("total_ask_size")
    val totalAskSize: BigDecimal,

    @get:JsonProperty("total_bid_size")
    val totalBidSize: BigDecimal,

    @get:JsonProperty("orderbook_units")
    val orderBookUnits: List<UpbitOrderBookUnit> = emptyList(),

    @get:JsonProperty("stream_type")
    val streamType: String
)