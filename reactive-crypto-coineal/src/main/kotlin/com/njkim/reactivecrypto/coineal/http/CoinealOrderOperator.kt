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

package com.njkim.reactivecrypto.coineal.http

import com.njkim.reactivecrypto.coineal.http.raw.CoinealRawOrderOperation
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderCancelResult
import com.njkim.reactivecrypto.core.common.model.order.OrderPlaceResult
import com.njkim.reactivecrypto.core.common.model.order.OrderStatus
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.model.paging.FirstPageRequest
import com.njkim.reactivecrypto.core.common.model.paging.NumberPageable
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.OrderOperation
import reactor.core.publisher.Mono
import java.math.BigDecimal

class CoinealOrderOperator(
    accessKey: String,
    secretKey: String,
    private val coinealRawOrderOperation: CoinealRawOrderOperation
) : OrderOperation(accessKey, secretKey) {
    override fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        return coinealRawOrderOperation
            .limitOrder(
                pair,
                tradeSideType,
                price,
                quantity
            )
            .map { OrderPlaceResult("${it.orderId}-$pair") }
    }

    override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
        val orderIdSplit = orderId.split("-", limit = 2)
        val coinealOrderId = orderIdSplit[0].toLong()
        val pair = CurrencyPair.parse(orderIdSplit[1])

        return coinealRawOrderOperation
            .cancelOrder(
                coinealOrderId,
                pair
            )
            .map { OrderCancelResult() }
    }

    override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>> {
        val numberPageable = when (pageable) {
            is FirstPageRequest -> pageable.toNumberPageable()
            is NumberPageable -> pageable
            else -> throw UnsupportedOperationException("coineal not allow ${pageable.javaClass.simpleName}")
        }

        return coinealRawOrderOperation
            .openOrders(pair, numberPageable.page, numberPageable.pageSize)
            .map { pageWrapper ->
                Page(
                    pageWrapper.resultList.map {
                        OrderStatus(
                            uniqueId = "${it.id}",
                            orderSideType = it.side,
                            currencyPair = pair,
                            price = it.price,
                            orderVolume = it.volume,
                            filledVolume = it.dealVolume,
                            createDateTime = it.createdAt
                        )
                    },
                    numberPageable.next()
                )
            }
    }

    override fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>> {
        val numberPageable = when (pageable) {
            is FirstPageRequest -> pageable.toNumberPageable()
            is NumberPageable -> pageable
            else -> throw UnsupportedOperationException("coineal not allow ${pageable.javaClass.simpleName}")
        }

        return coinealRawOrderOperation
            .tradeHistory(pair, numberPageable.page, numberPageable.pageSize)
            .map { pageWrapper ->
                Page(
                    pageWrapper.resultList.map {
                        TickData(
                            "${it.id}",
                            it.ctime,
                            it.price,
                            it.volume,
                            pair,
                            ExchangeVendor.COINEAL,
                            it.side
                        )
                    },
                    numberPageable.next()
                )
            }
    }
}