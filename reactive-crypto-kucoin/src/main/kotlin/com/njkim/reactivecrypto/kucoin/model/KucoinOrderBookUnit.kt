package com.njkim.reactivecrypto.kucoin.model

import java.math.BigDecimal

data class KucoinOrderBookUnit(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val sequence: BigDecimal?
)