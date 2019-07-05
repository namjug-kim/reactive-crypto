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

package com.njkim.reactivecrypto.upbit.http.raw

import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toMultiValueMap
import com.njkim.reactivecrypto.upbit.UpbitJsonObjectMapper
import com.njkim.reactivecrypto.upbit.model.UpbitOrderResponse
import com.njkim.reactivecrypto.upbit.model.UpbitOrderType
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal

class UpbitRawTradeOperator internal constructor(
    private val accessKey: String,
    private val secretKey: String,
    private val webClient: WebClient
) {

    fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<UpbitOrderResponse> {
        val limitRequest = mapOf(
            "market" to pair,
            "side" to tradeSideType,
            "volume" to quantity,
            "price" to price,
            "ord_type" to UpbitOrderType.LIMIT
        )

        return placeOrder(limitRequest)
    }

    fun marketOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        quantity: BigDecimal
    ): Mono<UpbitOrderResponse> {
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

        return placeOrder(marketRequest)
    }

    /**
     * @param icebergQty Used with LIMIT, STOP_LOSS_LIMIT, and TAKE_PROFIT_LIMIT to create an iceberg order.
     * @param stopPrice Used with STOP_LOSS, STOP_LOSS_LIMIT, TAKE_PROFIT, and TAKE_PROFIT_LIMIT orders.
     */
    private fun placeOrder(
        requestBodyMap: Map<String, Any>
    ): Mono<UpbitOrderResponse> {
        val upbitRequest = UpbitJsonObjectMapper.instance.convertValue<Map<String, Any>>(requestBodyMap)
        val sign = sign(upbitRequest, accessKey, secretKey)

        return webClient
            .post()
            .uri {
                it.path("/v1/orders")
                    .queryParams(upbitRequest.toMultiValueMap())
                    .build()
            }
            .header("Authorization", "Bearer $sign")
            .body(BodyInserters.fromObject(upbitRequest))
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono()
    }
}
