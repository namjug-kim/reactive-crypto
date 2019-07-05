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

package com.njkim.reactivecrypto.binance.http

import com.njkim.reactivecrypto.binance.http.raw.BinanceRawPrivateHttpClient
import com.njkim.reactivecrypto.core.http.AccountOperation
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.njkim.reactivecrypto.core.http.PrivateHttpClient

class BinancePrivateHttpClient(
    accessKey: String,
    secretKey: String,
    private val binanceRawPrivateHttpClient: BinanceRawPrivateHttpClient
) : PrivateHttpClient(accessKey, secretKey) {
    override fun account(): AccountOperation {
        return BinanceAccountOperation(accessKey, secretKey, binanceRawPrivateHttpClient)
    }

    override fun order(): OrderOperation {
        return BinanceOrderOperation(accessKey, secretKey, binanceRawPrivateHttpClient)
    }
}
