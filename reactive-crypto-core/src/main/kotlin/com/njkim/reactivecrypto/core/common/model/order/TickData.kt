package com.njkim.reactivecrypto.core.common.model.order

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

data class TickData(
    val uniqueId: String,
    val eventTime: ZonedDateTime,
    val price: BigDecimal,
    var quantity: BigDecimal,
    val currencyPair: CurrencyPair,
    val exchangeVendor: ExchangeVendor
)
