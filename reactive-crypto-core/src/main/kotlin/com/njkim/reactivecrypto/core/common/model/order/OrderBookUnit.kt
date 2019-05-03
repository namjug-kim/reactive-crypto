package com.njkim.reactivecrypto.core.common.model.order

import java.math.BigDecimal

data class OrderBookUnit(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val orderSideType: OrderSideType
)