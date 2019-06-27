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

import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.*
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.njkim.reactivecrypto.upbit.UpbitJsonObjectMapper
import com.njkim.reactivecrypto.upbit.http.raw.sign
import com.njkim.reactivecrypto.upbit.http.raw.upbitErrorHandling
import com.njkim.reactivecrypto.upbit.model.UpbitOrder
import com.njkim.reactivecrypto.upbit.model.UpbitOrderStatus
import com.njkim.reactivecrypto.upbit.model.UpbitOrderType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * @author traeper
 */
class UpbitOrderOperation(
    override val accessKey: String,
    override val secretKey: String,
    private val privateWebClient: WebClient
) : OrderOperation(accessKey, secretKey) {
    override fun orderStatus(orderId: String): Mono<OrderStatus> {
        val marketRequest =
            mapOf(
                "uuid" to orderId
            )

        val upbitRequest = UpbitJsonObjectMapper.instance.convertValue<Map<String, Any>>(marketRequest)
        val sign = sign(upbitRequest, accessKey, secretKey)

        return privateWebClient
            .post()
            .uri { it.path("/v1/order").build() }
            .header("Authorization", "Bearer $sign")
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono<UpbitOrderStatus>()
            .map {
                OrderStatus(
                    it.uuid,
                    it.orderStatusType,
                    it.side,
                    it.currencyPair,
                    it.price,
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
        TODO("not implemented")
    }

    override fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<OrderPlaceResult> {
        val marketRequest = when (tradeSideType) {
            TradeSideType.BUY -> {
                mapOf(
                    "market" to pair,
                    "side" to tradeSideType,
                    "price" to quantity,
                    "ord_type" to UpbitOrderType.PRICE
                )
            }
            else -> {
                mapOf(
                    "market" to pair,
                    "side" to tradeSideType,
                    "volume" to quantity,
                    "ord_type" to UpbitOrderType.MARKET
                )
            }
        }

        val upbitMarketRequest = UpbitJsonObjectMapper.instance.convertValue<Map<String, Any>>(marketRequest)
        val sign = sign(upbitMarketRequest, accessKey, secretKey)

        return privateWebClient
            .post()
            .uri { it.path("/v1/orders").build() }
            .header("Authorization", "Bearer $sign")
            .body(BodyInserters.fromObject(upbitMarketRequest))
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono<UpbitOrder>()
            .map {
                OrderPlaceResult(
                    it.uuid
                )
            }
    }

    override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
        TODO("not implemented")
    }

    override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>> {
        TODO("not implemented")
    }
}
