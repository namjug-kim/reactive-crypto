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

package com.njkim.reactivecrypto.binance.http

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.*
import com.njkim.reactivecrypto.core.common.model.paging.CursorPageable
import com.njkim.reactivecrypto.core.common.model.paging.FirstPageRequest
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.quantinel.remarketer.strategy.sdk.binance.BinanceRawPrivateHttpClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

class BinanceOrderOperation(
    accessKey: String,
    secretKey: String,
    private val binanceRawPrivateHttpClient: BinanceRawPrivateHttpClient
) : OrderOperation(accessKey, secretKey) {
    override fun orderStatus(orderId: String): Mono<OrderStatus> {
        val splits = orderId.split("-")
        val pair = CurrencyPair.parse(splits[0])
        val binanceOrderId = splits[1].toLong()

        return binanceRawPrivateHttpClient.userData()
            .order(pair, binanceOrderId)
            .map {
                OrderStatus(
                    createOrderId(it.symbol, it.orderId),
                    it.status.toOrderStatusType(),
                    if (it.side == TradeSideType.BUY) TradeSideType.BUY else TradeSideType.SELL,
                    it.symbol,
                    it.price,
                    it.origQty,
                    it.executedQty,
                    createDateTime = it.time
                )
            }
    }

    override fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return binanceRawPrivateHttpClient.trade()
            .limitOrder(pair, tradeSideType, quantity, TimeInForceType.GTC, price)
            .map { OrderPlaceResult("${it.orderId}") }
    }

    override fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return binanceRawPrivateHttpClient.trade()
            .marketOrder(pair, tradeSideType, quantity)
            .map { OrderPlaceResult(createOrderId(it.symbol, it.orderId)) }
    }

    /**
     * @param orderId $currencyPair-$originOrderId
     */
    override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
        val splits = orderId.split("-")
        val pair = CurrencyPair.parse(splits[0])
        val binanceOrderId = splits[1].toLong()

        return binanceRawPrivateHttpClient.trade()
            .cancelOrder(pair, binanceOrderId)
            .map { OrderCancelResult() }
    }

    override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>> {
        return binanceRawPrivateHttpClient
            .userData()
            .openOrders(pair)
            .map {
                OrderStatus(
                    createOrderId(it.symbol, it.orderId),
                    it.status.toOrderStatusType(),
                    if (it.side == TradeSideType.BUY) TradeSideType.BUY else TradeSideType.SELL,
                    it.symbol,
                    it.price,
                    it.origQty,
                    it.executedQty,
                    createDateTime = it.time
                )
            }
            .buffer()
            .next()
            .map {
                Page(
                    it,
                    FirstPageRequest(0)
                )
            }
    }

    override fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>> {
        val cursorPageable = when (pageable) {
            is FirstPageRequest -> pageable.toCursorPageable()
            is CursorPageable -> pageable.next() as CursorPageable
            else -> throw UnsupportedOperationException("not allow ${pageable.javaClass.simpleName}")
        }

        return binanceRawPrivateHttpClient
            .userData()
            .myTrades(pair, fromId = cursorPageable.cursor?.toLong())
            .map {
                TickData(
                    "${it.id}",
                    it.time,
                    it.price,
                    it.qty,
                    it.symbol,
                    ExchangeVendor.BINANCE,
                    if (it.isBuyer) TradeSideType.BUY else TradeSideType.SELL
                )
            }
            .buffer()
            .next()
            .map {
                Page(
                    it,
                    CursorPageable(cursorPageable.cursor, it.lastOrNull()?.uniqueId, 500)
                )
            }
    }

    private fun createOrderId(pair: CurrencyPair, orderId: Long): String {
        return "$pair-$orderId"
    }
}
