package com.njkim.reactivecrypto.hubi.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class HubiTickDataWrapper(
    @get:JsonProperty("curentExchangePrice")
    val curentExchangePrice: BigDecimal,

    @get:JsonProperty("up")
    val up: Boolean,

    @get:JsonProperty("summary24hour")
    val summary24hour: HubiOhlcv,

    @get:JsonProperty("trades")
    val trades: List<HubiTickData>
)

data class HubiTickData(
    @get:JsonProperty("amount")
    val amount: BigDecimal,

    @get:JsonProperty("channel")
    val channel: String,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("symbol")
    val symbol: CurrencyPair,

    @get:JsonProperty("time")
    val time: ZonedDateTime,

    @get:JsonProperty("type")
    val type: TradeSideType
)