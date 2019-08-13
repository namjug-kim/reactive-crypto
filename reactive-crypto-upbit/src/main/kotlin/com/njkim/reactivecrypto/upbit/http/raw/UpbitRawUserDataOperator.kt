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
import com.njkim.reactivecrypto.core.common.util.toMultiValueMap
import com.njkim.reactivecrypto.upbit.UpbitJsonObjectMapper
import com.njkim.reactivecrypto.upbit.model.UpbitBalance
import com.njkim.reactivecrypto.upbit.model.UpbitOrder
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class UpbitRawUserDataOperator internal constructor(
    private val accessKey: String,
    private val secretKey: String,
    private val webClient: WebClient
) {

    fun order(
        orderId: String
    ): Mono<UpbitOrder> {
        val request =
            mapOf(
                "uuid" to orderId
            )

        val upbitRequest = UpbitJsonObjectMapper.instance.convertValue<Map<String, Any>>(request)
        val sign = sign(upbitRequest, accessKey, secretKey)

        return webClient
            .get()
            .uri {
                it.path("/v1/order")
                    .queryParams(upbitRequest.toMultiValueMap())
                    .build()
            }
            .header("Authorization", "Bearer $sign")
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono()
    }

    fun balance(): Mono<List<UpbitBalance>> {
        val sign = sign(emptyMap(), accessKey, secretKey)

        return webClient
            .get()
            .uri {
                it.path("/v1/accounts")
                    .build()
            }
            .header("Authorization", "Bearer $sign")
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono()
    }
}
