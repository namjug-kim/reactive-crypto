package com.njkim.reactivecrypto.poloniex.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit

data class PoloniexOrderBookSnapshotEvent(
    override var currencyPair: CurrencyPair,
    val bids: List<OrderBookUnit>,
    val asks: List<OrderBookUnit>
) : PoloniexEvent(PoloniexEventType.ORDER_BOOK_SNAPSHOT)
