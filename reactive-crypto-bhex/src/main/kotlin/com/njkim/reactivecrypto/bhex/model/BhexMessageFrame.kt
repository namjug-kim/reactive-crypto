package com.njkim.reactivecrypto.bhex.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair

data class BhexMessageFrame<T>(
    val symbol: CurrencyPair,
    val topic: String,
    val data: T
)