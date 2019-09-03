package com.njkim.reactivecrypto.kucoin.http.raw

import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.kucoin.KucoinJsonObjectMapper
import com.njkim.reactivecrypto.kucoin.http.KucoinAuthUtil
import com.njkim.reactivecrypto.kucoin.model.KucoinHttpResponse
import com.njkim.reactivecrypto.kucoin.model.KucoinOrder
import com.njkim.reactivecrypto.kucoin.model.KucoinPlaceOrderResult
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant

class KucoinRawTradeOperator internal constructor(
    private val webClient: WebClient,
    private val accessKey: String,
    private val secretKey: String,
    private val passphrase: String
) {
    fun placeLimitOrder(
        clientOid: String,
        side: TradeSideType,
        symbol: CurrencyPair,
        price: BigDecimal,
        size: BigDecimal
    ): Mono<KucoinPlaceOrderResult> {
        val requestBody = mapOf(
            "clientOid" to clientOid,
            "side" to side,
            "type" to OrderType.LIMIT,
            "symbol" to symbol,
            "price" to price,
            "size" to size
        )

        val convertedRequestBody = KucoinJsonObjectMapper.instance.convertValue<Map<String, Any>>(requestBody)
        val method = HttpMethod.POST
        val path = "/api/v1/orders"
        val timestamp = Instant.now().toEpochMilli()
        val sign = KucoinAuthUtil.sign(secretKey, method, path, timestamp, convertedRequestBody)

        return webClient
            .method(method)
            .uri { it.path(path).build() }
            .headers {
                it.add("KC-API-KEY", accessKey)
                it.add("KC-API-SIGN", sign)
                it.add("KC-API-TIMESTAMP", "$timestamp")
                it.add("KC-API-PASSPHRASE", passphrase)
            }
            .body(BodyInserters.fromObject(convertedRequestBody))
            .retrieve()
            .bodyToMono<KucoinHttpResponse<KucoinPlaceOrderResult>>()
            .map { it.data }
    }

    fun getOrder(orderId: String): Mono<KucoinOrder> {
        val method = HttpMethod.GET
        val path = "/api/v1/orders/$orderId"
        val timestamp = Instant.now().toEpochMilli()
        val sign = KucoinAuthUtil.sign(secretKey, method, path, timestamp)

        return webClient
            .method(method)
            .uri { it.path(path).build() }
            .headers {
                it.add("KC-API-KEY", accessKey)
                it.add("KC-API-SIGN", sign)
                it.add("KC-API-TIMESTAMP", "$timestamp")
                it.add("KC-API-PASSPHRASE", passphrase)
            }
            .retrieve()
            .bodyToMono<KucoinHttpResponse<KucoinOrder>>()
            .map { it.data }
    }
}
