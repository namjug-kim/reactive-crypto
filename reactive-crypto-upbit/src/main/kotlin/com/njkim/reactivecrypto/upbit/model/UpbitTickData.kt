package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class UpbitTickData(
    @get:JsonProperty("type")
    val type: String,

    @get:JsonProperty("code")
    val code: CurrencyPair,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("trade_date")
    val tradeDate: String,

    @get:JsonProperty("trade_time")
    val tradeTime: String,

    @get:JsonProperty("trade_timestamp")
    val tradeTimestamp: ZonedDateTime,

    @get:JsonProperty("trade_price")
    val tradePrice: BigDecimal,

    @get:JsonProperty("trade_volume")
    val tradeVolume: BigDecimal,

    @get:JsonProperty("ask_bid")
    val askBid: String,

    @get:JsonProperty("prev_closing_price")
    val prevClosingPrice: BigDecimal,

    @get:JsonProperty("change")
    val change: String,

    @get:JsonProperty("change_price")
    val changePrice: BigDecimal,

    @get:JsonProperty("sequential_id")
    val sequentialId: Long,

    @get:JsonProperty("stream_type")
    val streamType: String
)