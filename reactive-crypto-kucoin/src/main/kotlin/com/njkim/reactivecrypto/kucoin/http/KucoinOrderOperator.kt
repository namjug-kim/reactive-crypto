package com.njkim.reactivecrypto.kucoin.http

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.*
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawTradeOperator
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.util.*

class KucoinOrderOperator internal constructor(
    private val kucoinRawTradeOperator: KucoinRawTradeOperator
) : OrderOperation("", "") {
    override fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return kucoinRawTradeOperator
            .placeLimitOrder(UUID.randomUUID().toString(), tradeSideType, pair, price, quantity)
            .map { OrderPlaceResult(it.orderId) }
    }

    override fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        TODO("not implemented")
    }

    override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
        TODO("not implemented")
    }

    override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<Order>> {
        TODO("not implemented")
    }

    override fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>> {
        TODO("not implemented")
    }

    override fun getOrder(orderId: String): Mono<Order> {
        return kucoinRawTradeOperator
            .getOrder(orderId)
            .map {
                val orderStatusType = if (it.dealSize <= BigDecimal.ZERO) {
                    OrderStatusType.NEW
                } else if (it.dealSize < it.size) {
                    OrderStatusType.PARTIALLY_FILLED
                } else if (!it.isActive) {
                    OrderStatusType.CANCELED
                } else {
                    OrderStatusType.FILLED
                }
                Order(
                    it.id,
                    orderStatusType,
                    it.side,
                    it.symbol,
                    it.price,
                    if (it.dealSize <= BigDecimal.ZERO) BigDecimal.ZERO else it.dealFunds / it.dealSize,
                    it.size,
                    it.dealSize,
                    createDateTime = it.createdAt
                )
            }
    }
}
