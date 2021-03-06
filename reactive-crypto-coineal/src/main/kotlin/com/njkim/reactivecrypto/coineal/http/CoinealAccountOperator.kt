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

import com.njkim.reactivecrypto.coineal.http.raw.CoinealRawAccountOperation
import com.njkim.reactivecrypto.core.common.model.account.Balance
import com.njkim.reactivecrypto.core.http.AccountOperation
import reactor.core.publisher.Flux

class CoinealAccountOperator(
    accessKey: String,
    secretKey: String,
    private val coinealRawAccountOperation: CoinealRawAccountOperation
) : AccountOperation(accessKey, secretKey) {
    override fun balance(): Flux<Balance> {
        return coinealRawAccountOperation
            .balance()
            .map {
                Balance(
                    it.currency,
                    it.normal,
                    it.locked
                )
            }
    }
}
