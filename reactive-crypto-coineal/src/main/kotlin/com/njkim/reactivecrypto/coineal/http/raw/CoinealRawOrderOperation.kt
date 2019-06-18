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

package com.njkim.reactivecrypto.coineal.http.raw

import com.njkim.reactivecrypto.coineal.model.CoinealApiResponse
import com.njkim.reactivecrypto.coineal.model.CoinealOpenOrder
import com.njkim.reactivecrypto.coineal.model.CoinealOrderResult
import com.njkim.reactivecrypto.coineal.model.CoinealPageWrapper
import com.njkim.reactivecrypto.coineal.model.CoinealTradeResult
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.common.util.toMultiValueMap
import mu.KotlinLogging
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.ZonedDateTime

class CoinealRawOrderOperation(
    private val webClient: WebClient,
    private val accessKey: String,
    private val secretKey: String
) {
    private val log = KotlinLogging.logger {}

    fun cancelOrder(orderId: Long, pair: CurrencyPair): Mono<Void> {
        var params = mapOf(
            "order_id" to orderId,
            "symbol" to pair,
            "api_key" to accessKey,
            "time" to ZonedDateTime.now().toEpochMilli()
        )
        params = sign(params, secretKey)
        return webClient.post()
            .uri { it.path("/open/api/cancel_order").build() }
            .body(BodyInserters.fromFormData(params.toMultiValueMap()))
            .retrieve()
            .bodyToMono<CoinealApiResponse<Void>>()
            // TODO check response code,msg
            .then()
    }

    fun openOrders(
        pair: CurrencyPair,
        page: Int,
        pageSize: Int = 100
    ): Mono<CoinealPageWrapper<CoinealOpenOrder>> {
        var params = mapOf(
            "symbol" to pair,
            "api_key" to accessKey,
            "time" to ZonedDateTime.now().toEpochMilli(),
            "page" to page,
            "pageSize" to pageSize
        )
        params = sign(params, secretKey)
        return webClient.get()
            .uri {
                it.path("/open/api/new_order")
                    .queryParams(params.toMultiValueMap())
                    .build()
            }
            .retrieve()
            .bodyToMono<CoinealApiResponse<CoinealPageWrapper<CoinealOpenOrder>>>()
            // TODO check response code,msg
            .map { it.data }
    }

    fun tradeHistory(
        pair: CurrencyPair,
        page: Int,
        pageSize: Int = 100
    ): Mono<CoinealPageWrapper<CoinealTradeResult>> {
        var params = mapOf(
            "symbol" to pair,
            "api_key" to accessKey,
            "time" to ZonedDateTime.now().toEpochMilli(),
            "page" to page,
            "page" to pageSize
        )
        params = sign(params, secretKey)
        return webClient.get()
            .uri {
                it.path("/open/api/all_trade")
                    .queryParams(params.toMultiValueMap())
                    .build()
            }
            .retrieve()
            .bodyToMono<CoinealApiResponse<CoinealPageWrapper<CoinealTradeResult>>>()
            .map { it.data }
    }

    fun limitOrder(
        pair: CurrencyPair,
        tradeSideType: TradeSideType,
        price: BigDecimal,
        quantity: BigDecimal
    ): Mono<CoinealOrderResult> {
        var params = mapOf(
            "time" to ZonedDateTime.now().toEpochMilli(),
            "side" to tradeSideType,
            "type" to 1,
            "price" to price,
            "volume" to quantity,
            "symbol" to pair,
            "api_key" to accessKey
        )
        params = sign(params, secretKey)

        return webClient.post()
            .uri { it.path("/open/api/create_order").build() }
            .body(BodyInserters.fromFormData(params.toMultiValueMap()))
            .retrieve()
            .bodyToMono<CoinealApiResponse<CoinealOrderResult>>()
            .map { it.data }
    }
}
