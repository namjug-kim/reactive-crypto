package com.njkim.reactivecrypto.core.common.model.order

import java.math.BigDecimal

/**
 * @property orderNumbers the number of orders placed at limit order.
 */
data class OrderBookUnit(
    val price: BigDecimal,
    val quantity: BigDecimal,
    val orderSideType: OrderSideType,
    val orderNumbers: Int?
)