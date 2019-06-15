package com.njkim.reactivecrypto.poloniex.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair

open class PoloniexEvent(
    open val eventType: PoloniexEventType
) {
    open lateinit var currencyPair: CurrencyPair
}
