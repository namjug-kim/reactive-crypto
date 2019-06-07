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

package com.njkim.reactivecrypto.upbit

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.njkim.reactivecrypto.core.common.model.account.Balance
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.*
import com.njkim.reactivecrypto.core.common.model.paging.Page
import com.njkim.reactivecrypto.core.common.model.paging.Pageable
import com.njkim.reactivecrypto.core.http.*
import com.njkim.reactivecrypto.upbit.model.UpbitApiException
import com.njkim.reactivecrypto.upbit.model.UpbitBalance
import com.njkim.reactivecrypto.upbit.model.UpbitErrorResponse
import com.njkim.reactivecrypto.upbit.model.UpbitOrder
import org.apache.commons.lang3.StringUtils
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeTypeUtils
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigInteger
import java.security.MessageDigest
import java.time.Instant
import org.springframework.web.reactive.function.BodyInserters
import java.net.URLEncoder


class UpbitHttpClient : ExchangeHttpClient() {
    private val baseUrl = "https://api.upbit.com"

    override fun privateApi(
        accessKey: String,
        secretKey: String,
        webClientBuilder: WebClient.Builder
    ): PrivateHttpClient {
        return UpbitPrivateHttpClient(accessKey, secretKey, webClientBuilder.build())
    }

    override fun publicApi(webClientBuilder: WebClient.Builder): PublicHttpClient {
        throw UnsupportedOperationException()
    }

    inner class UpbitPrivateHttpClient(
        override val accessKey: String,
        override val secretKey: String,
        override val webClient: WebClient
    ) : PrivateHttpClient(accessKey, secretKey, webClient) {

        override fun account(): AccountOperation {
            return UpbitAccountOperation(accessKey, secretKey, webClient)
        }

        override fun order(): OrderOperation {
            return UpbitOrderOperation(accessKey, secretKey, webClient)
        }
    }

    inner class UpbitAccountOperation(
        override val accessKey: String,
        override val secretKey: String,
        override val webClient: WebClient
    ) : AccountOperation(accessKey, secretKey, webClient) {
        override fun balance(): List<Balance> {
            val uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/v1/accounts")
                .encode()
                .build()
                .toUri()

            return createPrivateWebClient(accessKey, secretKey)
                .get()
                .uri(uri)
                .retrieve()
                .upbitErrorHandling()
                .bodyToMono<List<UpbitBalance>>()
                .block()!!
                .map {
                    Balance(
                        it.currency,
                        it.balance,
                        it.locked
                    )
                }
        }
    }

    inner class UpbitOrderOperation(
        override val accessKey: String,
        override val secretKey: String,
        override val webClient: WebClient
    ) : OrderOperation(accessKey, secretKey, webClient) {
        override fun limitOrder(
            pair: CurrencyPair,
            tradeSideType: TradeSideType,
            price: BigDecimal,
            quantity: BigDecimal
        ): Mono<OrderPlaceResult> {
            throw UnsupportedOperationException()
        }

        override fun marketOrder(
            pair: CurrencyPair,
            tradeSideType: TradeSideType,
            price: BigDecimal,
            quantity: BigDecimal
        ): Mono<OrderPlaceResult> {
            val uri = UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/v1/orders")
                .encode()
                .build()
                .toUri()

            val body: MutableMap<String, String> = getMarketOrderBody(pair, tradeSideType, price, quantity)
            val queryString = pairsToUrlQueryString(body)

            return createPrivateWebClient(accessKey, secretKey, queryString)
                .post()
                .uri(uri)
                .body(BodyInserters.fromObject(body))
                .retrieve()
                .upbitErrorHandling()
                .bodyToMono<UpbitOrder>()
                .map {
                    OrderPlaceResult(
                        it.uuid
                    )
                }
                // TODO Mono vs 일반 객체 중 어떤 것으로 리턴해야할지 결정 필요
        }

        override fun cancelOrder(orderId: String): Mono<OrderCancelResult> {
            throw UnsupportedOperationException()
        }

        override fun openOrders(pair: CurrencyPair, pageable: Pageable): Mono<Page<OrderStatus>> {
            throw UnsupportedOperationException()
        }

        override fun tradeHistory(): Mono<Page<TickData>> {
            throw UnsupportedOperationException()
        }

        private fun getMarketOrderBody(pair: CurrencyPair, tradeSideType: TradeSideType, price: BigDecimal, quantity: BigDecimal):
                MutableMap<String, String>
        {
            val body: MutableMap<String, String> = mutableMapOf()
            body["market"] = "${pair.baseCurrency}-${pair.targetCurrency}"
            body["side"] = if (tradeSideType == TradeSideType.BUY) {
                "bid"
            } else {
                "ask"
            }

            if(tradeSideType == TradeSideType.BUY) {
                body["price"] = price.toString()
                body["ord_type"] = "price"
            } else {
                body["volume"] = quantity.toString()
                body["ord_type"] = "market"
            }

            return body
        }

    }

    private fun createPrivateWebClient(accessKey: String, secretKey: String): WebClient {
        return createPrivateWebClient(accessKey, secretKey, StringUtils.EMPTY)
    }

    private fun createPrivateWebClient(accessKey: String, secretKey: String, queryString: String): WebClient {
        val algorithm = Algorithm.HMAC256(secretKey)

        val md = MessageDigest.getInstance("SHA-512")
        md.update(queryString.toByteArray());

        val queryHash = String.format("%0128x", BigInteger(1, md.digest()))

        val builder = JWT.create()
            .withClaim("access_key", accessKey)
            .withClaim("nonce", Instant.now().toEpochMilli().toString())

        if (StringUtils.isNotEmpty(queryString)) {
            builder
                .withClaim("query_hash", queryHash)
                .withClaim("query_hash_alg", "SHA512")
        }

        val token = builder.sign(algorithm)

        return defaultWebClientBuilder()
            .defaultHeader("Authorization", "Bearer $token")
            .build()
    }

    private fun defaultWebClientBuilder(): WebClient.Builder {
        val strategies = ExchangeStrategies.builder()
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(
                        Jackson2JsonEncoder(
                            UpbitJsonObjectMapper().objectMapper(),
                            MimeTypeUtils.APPLICATION_JSON
                        )
                    )
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(
                        Jackson2JsonDecoder(
                            UpbitJsonObjectMapper().objectMapper(),
                            MimeTypeUtils.APPLICATION_JSON
                        )
                    )
            }
            .build()

        return WebClient.builder()
            .exchangeStrategies(strategies)
    }
}

fun pairsToUrlQueryString(pairs: MutableMap<String, String>): String {
    return pairs.entries
        .map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
        .joinToString("&")
}

fun WebClient.ResponseSpec.upbitErrorHandling(): WebClient.ResponseSpec = onStatus({ it.isError }, { clientResponse ->
    clientResponse.bodyToMono(UpbitErrorResponse::class.java)
        .map { UpbitApiException(clientResponse.statusCode(), it) }
})