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

import com.njkim.reactivecrypto.coineal.CoinealJsonObjectMapper
import com.njkim.reactivecrypto.core.http.PublicHttpClient
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeTypeUtils
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient

class CoinealRawHttpClient {
    private val webClient = createWebClient()

    fun privateApi(
        accessKey: String,
        secretKey: String
    ): CoinealRawPrivateHttpClient {
        return CoinealRawPrivateHttpClient(accessKey, secretKey, webClient)
    }

    fun publicApi(): PublicHttpClient {
        TODO("not implemented")
    }

    private fun createWebClient(): WebClient {
        val strategies = ExchangeStrategies.builder()
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(
                        Jackson2JsonEncoder(
                            CoinealJsonObjectMapper().objectMapper(),
                            MimeTypeUtils.APPLICATION_JSON
                        )
                    )
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(
                        Jackson2JsonDecoder(
                            CoinealJsonObjectMapper().objectMapper(),
                            MimeTypeUtils.APPLICATION_JSON
                        )
                    )
            }
            .build()

        return WebClient.builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient.create().wiretap(true)
                )
            )
            .exchangeStrategies(strategies)
            .baseUrl("https://exchange-open-api.coineal.com/")
            .build()
    }
}
