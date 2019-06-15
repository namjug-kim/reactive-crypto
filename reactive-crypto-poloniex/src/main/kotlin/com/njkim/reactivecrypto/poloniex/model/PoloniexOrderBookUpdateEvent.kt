package com.njkim.reactivecrypto.poloniex.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import java.math.BigDecimal

data class PoloniexOrderBookUpdateEvent(
    val side: OrderSideType,
    val price: BigDecimal,
    val quantity: BigDecimal
) : PoloniexEvent(PoloniexEventType.ORDER_BOOK_UPDATE) {
    override lateinit var currencyPair: CurrencyPair
}
