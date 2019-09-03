package com.njkim.reactivecrypto.kucoin.http.raw

import com.njkim.reactivecrypto.kucoin.model.KucoinResponseBody
import com.njkim.reactivecrypto.kucoin.model.KucoinWebsocketAuthResponse
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class KucoinRawPublicAuthOperator internal constructor(
    private val webClient: WebClient
) {
    fun websocketAuth(): Mono<KucoinWebsocketAuthResponse> {
        return webClient
            .post()
            .uri { it.path("/api/v1/bullet-public").build() }
            .retrieve()
            .bodyToMono<KucoinResponseBody<KucoinWebsocketAuthResponse>>()
            .map { it.data }
    }
}