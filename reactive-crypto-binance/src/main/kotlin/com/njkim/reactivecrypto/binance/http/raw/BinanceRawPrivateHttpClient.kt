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

package com.quantinel.remarketer.strategy.sdk.binance

import com.njkim.reactivecrypto.binance.http.raw.BinanceRawUserDataOperator
import com.njkim.reactivecrypto.core.common.util.CryptUtil
import com.njkim.reactivecrypto.core.common.util.byteArrayToHex
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder

class BinanceRawPrivateHttpClient internal constructor(
    private val apiKey: String,
    private val secretKey: String,
    private val webClientBuilder: WebClient.Builder
) {
    private val authenticatedWebClient = createPrivateWebClient()

    fun trade(): BinanceRawTradeOperator {
        return BinanceRawTradeOperator(authenticatedWebClient)
    }

    fun userData(): BinanceRawUserDataOperator {
        return BinanceRawUserDataOperator(authenticatedWebClient)
    }

    private fun createPrivateWebClient(): WebClient {
        val binanceAuthenticationFilter = ExchangeFilterFunction { request, next ->
            val query = request.url()
                .query

            val signature = CryptUtil.encrypt("HmacSHA256", query.toByteArray(), secretKey.toByteArray())
                .byteArrayToHex()

            val uriWithSign = UriComponentsBuilder.fromUri(request.url())
                .queryParam("signature", signature)
                .build()
                .toUri()

            next.exchange(ClientRequest.from(request)
                .url(uriWithSign)
                .headers { headers -> headers.add("X-MBX-APIKEY", apiKey) }
                .build())
        }

        return webClientBuilder.filter(binanceAuthenticationFilter)
            .build()
    }
}
