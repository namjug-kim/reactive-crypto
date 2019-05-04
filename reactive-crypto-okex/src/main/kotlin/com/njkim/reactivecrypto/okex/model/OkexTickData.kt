package com.njkim.reactivecrypto.okex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * {
 * ”table”: "spot/trade”,
 * ”data ”: [
 * [
 * {
 * ”instrument_id”: "ETH-USDT”,
 * ”price”: "22888”,
 * ”side”: "buy”,
 * ”size”: "7”,
 * ”timestamp”: "2018-11-22T03:58:57.709Z”,
 * ”trade_id”: "108223090144493569”
 * }]
 * ]
 * }
 */
data class OkexTickDataWrapper(
    @get:JsonProperty("table")
    val table: String,

    @get:JsonProperty("data")
    val data: List<OkexTickData>
)

data class OkexTickData(
    @get:JsonProperty("instrument_id")
    val instrumentId: CurrencyPair,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("side")
    val side: TradeSideType,

    @get:JsonProperty("size")
    val size: BigDecimal,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("trade_id")
    val tradeId: String
)