package com.njkim.reactivecrypto.kucoin.http.raw

import org.springframework.web.reactive.function.client.WebClient

class KucoinRawPublicHttpClient internal constructor(
    private val webClient: WebClient
) {
    fun auth(): KucoinRawPublicAuthOperator {
        return KucoinRawPublicAuthOperator(webClient)
    }

    fun market(): KucoinRawMarketOperator {
        return KucoinRawMarketOperator(webClient)
    }
}