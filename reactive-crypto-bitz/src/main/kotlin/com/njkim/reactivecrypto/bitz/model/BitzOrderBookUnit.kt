package com.njkim.reactivecrypto.bitz.model

import java.math.BigDecimal

data class BitzOrderBookUnit(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val totalAmount: BigDecimal
)