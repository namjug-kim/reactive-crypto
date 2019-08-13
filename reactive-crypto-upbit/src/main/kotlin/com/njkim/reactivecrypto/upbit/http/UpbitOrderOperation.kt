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

package com.njkim.reactivecrypto.upbit.http

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.*
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.njkim.reactivecrypto.upbit.http.raw.UpbitRawPrivateHttpClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * @author traeper
 */
class UpbitOrderOperation(
    override val accessKey: String,
    override val secretKey: String,
    private val upbitRawPrivateHttpClient: UpbitRawPrivateHttpClient
) : OrderOperation(accessKey, secretKey) {
    override fun getOrder(orderId: String): Mono<Order> {
        return upbitRawPrivateHttpClient
            .userData()
            .order(orderId)
            .map {
                val averageTradePrice = when {
                    it.trades.isEmpty() -> null
                    else -> {
                        val totalFunds = it.trades.stream()
                            .map { trade -> trade.funds }
                            .reduce(BigDecimal.ZERO) { left, right -> left + right }

                        val totalVolume = it.trades.stream()
                            .map { trade -> trade.volume }
                            .reduce(BigDecimal.ZERO) { left, right -> left + right }

                        if (totalFunds > BigDecimal.ZERO) {
                            totalFunds / totalVolume
                        } else {
                            null
                        }
                    }
                }

                Order(
                    it.uuid,
                    it.upbitOrderStatusType.toOrderStatusType(it.volume, it.executedVolume),
                    it.side,
                    it.currencyPair,
                    it.price,
                    averageTradePrice,
                    it.volume,
                    it.executedVolume,
                    it.paidFee,
                    it.reservedFee,
                    it.remainingFee,
                    it.createdAt
                )
            }
    }

    override fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>> {
        TODO("not implemented")
    }

    override fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return upbitRawPrivateHttpClient
            .trade()
            .limitOrder(pair, tradeSideType, price, quantity)
            .map {
                OrderPlaceResult(
                    it.uuid
                )
            }
    }

    override fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return upbitRawPrivateHttpClient
            .trade()
            .marketOrder(pair, tradeSideType, quantity)
            .map {
                OrderPlaceResult(
                    it.uuid
                )
            }
    }

    override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
        TODO("not implemented")
    }

    override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<Order>> {
        TODO("not implemented")
    }
}
