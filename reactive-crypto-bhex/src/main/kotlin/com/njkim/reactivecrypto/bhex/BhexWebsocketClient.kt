package com.njkim.reactivecrypto.bhex

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient
import reactor.core.publisher.Flux

class BhexWebsocketClient(host: String = "ws.bhex.com") : ExchangePublicWebsocketClient {
    private val bhexRawWebsocketClient = BhexRawWebsocketClient(host)

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        return bhexRawWebsocketClient.createDepthFlux(subscribeTargets)
            .flatMapIterable { it.data }
            .map {
                OrderBook(
                    "${it.eventTime.toEpochMilli()}",
                    it.symbol,
                    it.eventTime,
                    ExchangeVendor.BHEX,
                    it.bids.map { bid -> OrderBookUnit(bid.price, bid.quantity, TradeSideType.BUY) },
                    it.asks.map { ask -> OrderBookUnit(ask.price, ask.quantity, TradeSideType.SELL) }
                )
            }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return bhexRawWebsocketClient.createTradeFlux(subscribeTargets)
            .flatMapIterable { bhexMessageFrame ->
                bhexMessageFrame.data.map {
                    TickData(
                        it.version,
                        it.time,
                        it.price,
                        it.quantity,
                        bhexMessageFrame.symbol,
                        ExchangeVendor.BHEX,
                        it.side
                    )
                }
            }
    }
}
