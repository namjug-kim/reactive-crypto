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
import com.njkim.reactivecrypto.upbit.http.raw.UpbitRawPrivateHttpClient
import reactor.core.publisher.Flux

/**
 * @author traeper
 */
class UpbitAccountOperation(
    override val accessKey: String,
    override val secretKey: String,
    private val upbitRawPrivateHttpClient: UpbitRawPrivateHttpClient
) : AccountOperation(accessKey, secretKey) {
    override fun balance(): Flux<Balance> {
        return upbitRawPrivateHttpClient
            .userData()
            .balance()
            .flatMapMany {
                Flux.fromIterable(it)
            }
            .map {
                Balance(
                    it.currency,
                    it.balance,
                    it.locked
                )
            }
    }
}
