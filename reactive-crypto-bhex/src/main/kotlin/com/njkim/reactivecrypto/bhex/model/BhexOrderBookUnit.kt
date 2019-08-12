package com.njkim.reactivecrypto.bhex.model

import java.math.BigDecimal

data class BhexOrderBookUnit(
    val price: BigDecimal,
    val quantity: BigDecimal
)