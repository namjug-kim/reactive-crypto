package com.njkim.reactivecrypto.kucoin.http.raw

import org.springframework.web.reactive.function.client.WebClient

class KucoinRawPrivateHttpClient internal constructor(
    private val webClient: WebClient,
    private val accessKey: String,
    private val secretKey: String,
    private val passphrase: String
) {
    fun trade(): KucoinRawTradeOperator {
        return KucoinRawTradeOperator(webClient, accessKey, secretKey, passphrase)
    }

    fun account(): KucoinRawAccountOperator {
        return KucoinRawAccountOperator(webClient, accessKey, secretKey, passphrase)
    }
}
