package com.njkim.reactivecrypto.kucoin.http.raw

import com.njkim.reactivecrypto.kucoin.http.KucoinAuthUtil
import com.njkim.reactivecrypto.kucoin.model.KucoinAccount
import com.njkim.reactivecrypto.kucoin.model.KucoinHttpResponse
import org.springframework.http.HttpMethod
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import java.time.Instant

class KucoinRawAccountOperator internal constructor(
    private val webClient: WebClient,
    private val accessKey: String,
    private val secretKey: String,
    private val passphrase: String
) {
    fun getAccounts(): Flux<KucoinAccount> {
        val method = HttpMethod.GET
        val path = "/api/v1/accounts"
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
            .bodyToMono<KucoinHttpResponse<List<KucoinAccount>>>()
            .flatMapIterable { it.data }
    }
}
