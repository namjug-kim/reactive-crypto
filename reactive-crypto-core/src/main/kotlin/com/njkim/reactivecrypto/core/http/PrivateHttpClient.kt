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

package com.njkim.reactivecrypto.core.http

import com.njkim.reactivecrypto.core.common.model.account.Balance
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderCancelResult
import com.njkim.reactivecrypto.core.common.model.order.OrderPlaceResult
import com.njkim.reactivecrypto.core.common.model.order.OrderStatus
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal

abstract class PrivateHttpClient(
    protected open val accessKey: String,
    protected open val secretKey: String
) {
    abstract fun account(): AccountOperation
    abstract fun order(): OrderOperation
}

abstract class AccountOperation(
    protected open val accessKey: String,
    protected open val secretKey: String
) {
    abstract fun balance(): Flux<Balance>
}

abstract class OrderOperation(
    protected open val accessKey: String,
    protected open val secretKey: String
) {
    abstract fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult>

    /**
     * @param quantity SELL : quantity of targetCurrency, BUY : volume of baseCurrency
     */
    abstract fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult>

    abstract fun cancelOrder(orderId: String): Mono<OrderCancelResult>

    abstract fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>>

    abstract fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>>

    abstract fun orderStatus(pair: CurrencyPair, orderId: String): Mono<OrderStatus>
}
