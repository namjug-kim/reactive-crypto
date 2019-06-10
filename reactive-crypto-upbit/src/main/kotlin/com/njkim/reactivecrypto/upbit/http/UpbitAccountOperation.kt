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

import com.njkim.reactivecrypto.core.common.model.account.Balance
import com.njkim.reactivecrypto.core.http.AccountOperation
import com.njkim.reactivecrypto.upbit.http.raw.sign
import com.njkim.reactivecrypto.upbit.http.raw.upbitErrorHandling
import com.njkim.reactivecrypto.upbit.model.UpbitBalance
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux

/**
 * @author traeper
 */
class UpbitAccountOperation(
    override val accessKey: String,
    override val secretKey: String,
    private val privateWebClient: WebClient
) : AccountOperation(accessKey, secretKey) {
    override fun balance(): Flux<Balance> {
        val sign = sign(emptyMap(), accessKey, secretKey)

        return privateWebClient.get()
            .uri {
                it.path("/v1/accounts")
                    .build()
            }
            .header("Authorization", "Bearer $sign")
            .retrieve()
            .upbitErrorHandling()
            .bodyToMono<List<UpbitBalance>>()
            .flatMapIterable { upbitBalances ->
                upbitBalances.map {
                    Balance(
                        it.currency,
                        it.balance,
                        it.locked
                    )
                }
            }
    }
}
