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

package com.njkim.reactivecrypto.binance.http.raw

import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.binance.BinanceJsonObjectMapper
import com.njkim.reactivecrypto.binance.model.BinanceAccountResponse
import com.njkim.reactivecrypto.binance.model.BinanceOrderInfoResponse
import com.njkim.reactivecrypto.binance.model.BinanceTradeInfoResponse
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.toMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToFlux
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.ZonedDateTime

class BinanceRawUserDataOperator internal constructor(private val webClient: WebClient) {
    /**
     * Get current account information.
     */
    fun account(recvWindow: Long = 5000): Mono<BinanceAccountResponse> {
        val request = mapOf<String, Any>(
            "recvWindow" to recvWindow,
            "timestamp" to Instant.now().toEpochMilli() - 1000
        )

        val convertedRequest = BinanceJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
            .toMultiValueMap()

        return webClient.get()
            .uri {
                it.path("/api/v3/account")
                    .queryParams(convertedRequest)
                    .build()
            }
            .retrieve()
            .bodyToMono()
    }

    /**
     * Get all account orders; active, canceled, or filled.
     * For some historical orders cummulativeQuoteQty will be < 0, meaning the data is not available at this time.
     *
     * Weight: 5 with symbol
     *
     * @param orderId If orderId is set, it will get orders >= that orderId. Otherwise most recent orders are returned.
     */
    fun allOrders(
        symbol: CurrencyPair,
        orderId: Long? = null,
        startTime: ZonedDateTime? = null,
        endTime: ZonedDateTime? = null,
        limit: Int = 500,
        recvWindow: Long = 5000
    ): Flux<BinanceOrderInfoResponse> {
        val request = mapOf(
            "symbol" to symbol,
            "limit" to limit,
            "recvWindow" to recvWindow,
            "timestamp" to Instant.now().toEpochMilli()
        ).toMutableMap()
        orderId?.let { request["orderId"] = orderId }
        startTime?.let { request["startTime"] = startTime }
        endTime?.let { request["endTime"] = endTime }

        val convertedRequest = BinanceJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
            .toMultiValueMap()

        return webClient.get()
            .uri {
                it.path("/api/v3/allOrders")
                    .queryParams(convertedRequest)
                    .build()
            }
            .retrieve()
            .bodyToFlux()
    }

    /**
     * Get all open orders on a symbol. Careful when accessing this with no symbol.
     *
     * Weight: 1 for a single symbol; 40 when the symbol parameter is omitted
     *
     * @param symbol If the symbol is not sent, orders for all symbols will be returned in an array.
     */
    fun openOrders(
        symbol: CurrencyPair? = null,
        recvWindow: Long = 5000
    ): Flux<BinanceOrderInfoResponse> {
        val request = mapOf<String, Any>(
            "recvWindow" to recvWindow,
            "timestamp" to Instant.now().toEpochMilli()
        ).toMutableMap()
        symbol?.let { request["symbol"] = symbol }

        val convertedRequest = BinanceJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
            .toMultiValueMap()

        return webClient.get()
            .uri {
                it.path("/api/v3/openOrders")
                    .queryParams(convertedRequest)
                    .build()
            }
            .retrieve()
            .bodyToFlux()
    }

    /**
     * Check an order's status.
     *
     * Weight: 1
     *
     * Either orderId or origClientOrderId must be sent.
     * For some historical orders cummulativeQuoteQty will be < 0, meaning the data is not available at this time.
     */
    fun order(
        symbol: CurrencyPair,
        orderId: Long? = null,
        origClientOrderId: String? = null,
        recvWindow: Long = 5000
    ): Mono<BinanceOrderInfoResponse> {
        val request = mapOf(
            "symbol" to symbol,
            "recvWindow" to recvWindow,
            "timestamp" to Instant.now().toEpochMilli()
        ).toMutableMap()
        orderId?.let { request["orderId"] = orderId }
        origClientOrderId?.let { request["origClientOrderId"] = origClientOrderId }

        val convertedRequest = BinanceJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
            .toMultiValueMap()

        return webClient.get()
            .uri {
                it.path("/api/v3/order")
                    .queryParams(convertedRequest)
                    .build()
            }
            .retrieve()
            .bodyToMono()
    }

    /**
     * Get trades for a specific account and symbol.
     *
     * Weight: 5 with symbol
     *
     * @param fromId TradeId to fetch from. Default gets most recent trades.
     * If fromId is set, it will get orders >= that fromId. Otherwise most recent orders are returned.
     * @param limit Default 500; max 1000/
     */
    fun myTrades(
        symbol: CurrencyPair,
        startTime: ZonedDateTime? = null,
        endTime: ZonedDateTime? = null,
        fromId: Long? = null,
        limit: Int = 500,
        recvWindow: Long = 5000
    ): Flux<BinanceTradeInfoResponse> {
        val request = mapOf<String, Any>(
            "symbol" to symbol,
            "recvWindow" to recvWindow,
            "timestamp" to Instant.now().toEpochMilli(),
            "limit" to limit
        ).toMutableMap()
        startTime?.let { request["startTime"] = startTime }
        endTime?.let { request["endTime"] = endTime }
        fromId?.let { request["fromId"] = fromId }

        val convertedRequest = BinanceJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
            .toMultiValueMap()

        return webClient.get()
            .uri {
                it.path("/api/v3/myTrades")
                    .queryParams(convertedRequest)
                    .build()
            }
            .retrieve()
            .bodyToFlux()
    }
}
