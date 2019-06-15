/*
 * Copyright 2019 namjug-kim
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.njkim.reactivecrypto.poloniex

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import com.njkim.reactivecrypto.poloniex.model.PoloniexEventType
import com.njkim.reactivecrypto.poloniex.model.PoloniexOrderBookSnapshotEvent
import com.njkim.reactivecrypto.poloniex.model.PoloniexOrderBookUpdateEvent
import com.njkim.reactivecrypto.poloniex.model.PoloniexTradeEvent
import reactor.core.publisher.Flux
import java.math.BigDecimal
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * poloniex websocket version 2
 * document : https://docs.poloniex.com/#websocket-api
 */
class PoloniexWebsocketClient : ExchangeWebsocketClient {
    private val poloniexRawWebsocketClient: PoloniexRawWebsocketClient = PoloniexRawWebsocketClient()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        return poloniexRawWebsocketClient
            .priceAggregatedBook(subscribeTargets)
            .flatMapIterable { it.events }
            .filter { it.eventType == PoloniexEventType.TRADE }
            .map { it as PoloniexTradeEvent }
            .map { poloniexTradeEvent ->
                TickData(
                    poloniexTradeEvent.tradeId,
                    poloniexTradeEvent.eventTime,
                    poloniexTradeEvent.price,
                    poloniexTradeEvent.size,
                    poloniexTradeEvent.currencyPair,
                    ExchangeVendor.POLONIEX,
                    poloniexTradeEvent.side
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        return poloniexRawWebsocketClient
            .priceAggregatedBook(subscribeTargets)
            .flatMapIterable { it.events }
            .filter {
                it.eventType == PoloniexEventType.ORDER_BOOK_SNAPSHOT ||
                        it.eventType == PoloniexEventType.ORDER_BOOK_UPDATE
            }
            .map { event ->
                if (event.eventType == PoloniexEventType.ORDER_BOOK_SNAPSHOT) {
                    val now = ZonedDateTime.now()
                    val orderBookSnapshotEvent = event as PoloniexOrderBookSnapshotEvent
                    val orderBook = OrderBook(
                        "${now.toEpochMilli()}",
                        event.currencyPair,
                        now,
                        ExchangeVendor.POLONIEX,
                        orderBookSnapshotEvent.bids,
                        orderBookSnapshotEvent.asks
                    )
                    orderBook
                } else {
                    val now = ZonedDateTime.now()
                    val orderBookUpdateEvent = event as PoloniexOrderBookUpdateEvent
                    val prevOrderBook = currentOrderBookMap[event.currencyPair]!!

                    var bids = prevOrderBook.bids.toMutableList()
                    var asks = prevOrderBook.asks.toMutableList()

                    if (orderBookUpdateEvent.side == OrderSideType.BID) {
                        bids = applyOrderBookUpdate(bids, orderBookUpdateEvent)
                    } else {
                        asks = applyOrderBookUpdate(asks, orderBookUpdateEvent)
                    }

                    prevOrderBook.copy(
                        uniqueId = "${now.toEpochMilli()}",
                        eventTime = now,
                        asks = asks,
                        bids = bids
                    )
                }
            }
            .doOnNext { currentOrderBookMap[it.currencyPair] = it }
            .doFinally { currentOrderBookMap.clear() }
    }

    private fun applyOrderBookUpdate(
        orderBookUnits: MutableList<OrderBookUnit>,
        orderBookUpdateEvent: PoloniexOrderBookUpdateEvent
    ): MutableList<OrderBookUnit> {
        val updated: MutableList<OrderBookUnit>
        var isNewPrice = true
        updated = orderBookUnits
            .map { orderBookUnit ->
                if (orderBookUnit.price.compareTo(orderBookUpdateEvent.price) == 0) {
                    isNewPrice = false
                    orderBookUnit.copy(quantity = orderBookUpdateEvent.quantity)
                } else {
                    orderBookUnit
                }
            }
            .filter { it.quantity > BigDecimal.ZERO }
            .toMutableList()

        if (isNewPrice) {
            updated.add(
                OrderBookUnit(
                    orderBookUpdateEvent.price,
                    orderBookUpdateEvent.quantity,
                    orderBookUpdateEvent.side
                )
            )
        }
        return updated
    }
}
