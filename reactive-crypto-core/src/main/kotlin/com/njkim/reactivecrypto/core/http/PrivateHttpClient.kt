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
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.math.BigDecimal

abstract class PrivateHttpClient(
    protected val accessKey: String,
    protected val secretKey: String
) {
    abstract fun account(): AccountOperation
    abstract fun order(): OrderOperation
}

abstract class AccountOperation(
    protected val accessKey: String,
    protected val secretKey: String
) {
    abstract fun balance(): List<Balance>
}

abstract class OrderOperation(
    protected val accessKey: String,
    protected val secretKey: String
) {
    abstract fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult>

    /**
     * 시장가 매매
     * - 시장가 매수 : pair, tradeSideType, price 필수, quantity 무시
     * - 시장가 매도 : pair, tradeSideType, quantity 필수, price 무시
     */
    abstract fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult>

    abstract fun cancelOrder(orderId: String): Mono<OrderCancelResult>

    abstract fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>>

    abstract fun tradeHistory(pair: CurrencyPair, pageable: Pageable): Mono<Page<TickData>>
}
