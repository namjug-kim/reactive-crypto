package com.njkim.reactivecrypto.bitz

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.util.concurrent.ConcurrentHashMap

class BitzWebsocketClient(
    host: String = "ws.ahighapi.com",
    cdid: String = "100002"
) : ExchangePublicWebsocketClient {
    private val bitzRawWebsocketClient = BitzRawWebsocketClient(host, cdid)

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return bitzRawWebsocketClient
            .createTradeFlux(subscribeTargets)
            .flatMapIterable { bitzMessageFrame ->
                bitzMessageFrame.data.map {
                    TickData(
                        "${it.id}",
                        it.timestamp,
                        it.price,
                        it.quantity,
                        bitzMessageFrame.params.symbol,
                        ExchangeVendor.BITZ,
                        it.side
                    )
                }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        return bitzRawWebsocketClient.createDepthFlux(subscribeTargets)
            .map { bitzMessageFrame ->
                OrderBook(
                    "${bitzMessageFrame.time.toEpochMilli()}",
                    bitzMessageFrame.params.symbol,
                    bitzMessageFrame.time,
                    ExchangeVendor.BITZ,
                    bitzMessageFrame.data.bids.map { bid ->
                        OrderBookUnit(
                            bid.price,
                            bid.quantity,
                            TradeSideType.BUY
                        )
                    },
                    bitzMessageFrame.data.asks.map { ask ->
                        OrderBookUnit(
                            ask.price,
                            ask.quantity,
                            TradeSideType.SELL
                        )
                    }
                )
            }
            .map { orderBook ->
                if (!currentOrderBookMap.containsKey(orderBook.currencyPair)) {
                    currentOrderBookMap[orderBook.currencyPair] = orderBook
                    return@map orderBook
                }

                val prevOrderBook = currentOrderBookMap[orderBook.currencyPair]!!

                val askMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.asks
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.asks.forEach { updatedAsk ->
                    askMap.compute(updatedAsk.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedAsk.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedAsk
                            else -> oldValue.copy(
                                quantity = updatedAsk.quantity,
                                orderNumbers = updatedAsk.orderNumbers
                            )
                        }
                    }
                }

                val bidMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.bids
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.bids.forEach { updatedBid ->
                    bidMap.compute(updatedBid.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedBid.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedBid
                            else -> oldValue.copy(
                                quantity = updatedBid.quantity,
                                orderNumbers = updatedBid.orderNumbers
                            )
                        }
                    }
                }

                val currentOrderBook = prevOrderBook.copy(
                    eventTime = orderBook.eventTime,
                    asks = askMap.values.sortedBy { orderBookUnit -> orderBookUnit.price },
                    bids = bidMap.values.sortedByDescending { orderBookUnit -> orderBookUnit.price }
                )
                currentOrderBookMap[currentOrderBook.currencyPair] = currentOrderBook
                currentOrderBook
            }
            .doFinally { currentOrderBookMap.clear() } // cleanup memory limit orderBook when disconnected
    }
}
