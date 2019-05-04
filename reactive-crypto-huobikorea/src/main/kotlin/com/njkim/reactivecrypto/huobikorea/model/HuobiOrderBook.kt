package com.njkim.reactivecrypto.huobikorea.model

import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import java.math.BigDecimal
import kotlin.streams.toList

data class HuobiOrderBook(
    private val bids: List<List<String>>,
    private val asks: List<List<String>>
) {
    fun getBids(): List<OrderBookUnit> {
        return bids.stream()
            .map { objects ->
                OrderBookUnit(
                    BigDecimal(objects[0]),
                    BigDecimal(objects[1]),
                    OrderSideType.BID,
                    null
                )
            }
            .toList()
    }

    fun getAsks(): List<OrderBookUnit> {
        return asks.stream()
            .map { objects ->
                OrderBookUnit(
                    BigDecimal(objects[0]),
                    BigDecimal(objects[1]),
                    OrderSideType.ASK,
                    null
                )
            }
            .toList()
    }
}
