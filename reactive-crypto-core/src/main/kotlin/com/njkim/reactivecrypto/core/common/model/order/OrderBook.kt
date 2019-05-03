package com.njkim.reactivecrypto.core.common.model.order

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.time.ZonedDateTime

data class OrderBook(
    val uniqueId: String,
    val currencyPair: CurrencyPair,
    val eventTime: ZonedDateTime,
    val exchangeVendor: ExchangeVendor,
    val bids: List<OrderBookUnit>,
    val asks: List<OrderBookUnit>
)