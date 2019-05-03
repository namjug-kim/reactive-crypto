package com.njkim.reactivecrypto.core

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import reactor.core.publisher.Flux

interface ExchangeWebsocketClient {
    fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData>
    fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook>
}