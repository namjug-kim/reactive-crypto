package com.njkim.reactivecrypto.poloniex.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class PoloniexTradeEvent(
    val tradeId: String,
    val side: TradeSideType,
    val price: BigDecimal,
    val size: BigDecimal,
    val eventTime: ZonedDateTime
) : PoloniexEvent(PoloniexEventType.TRADE) {
    override lateinit var currencyPair: CurrencyPair
}
