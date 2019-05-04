package com.njkim.reactivecrypto.bithumb.model

import com.njkim.reactivecrypto.core.common.model.currency.Currency

data class BithumbResponseHeader(
    val currency: Currency,
    val service: String
)