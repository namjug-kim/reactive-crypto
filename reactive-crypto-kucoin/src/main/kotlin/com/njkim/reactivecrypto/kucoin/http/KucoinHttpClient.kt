package com.njkim.reactivecrypto.kucoin.http

import com.njkim.reactivecrypto.core.http.ExchangeHttpClient
import com.njkim.reactivecrypto.core.http.PrivateHttpClient
import com.njkim.reactivecrypto.core.http.PublicHttpClient
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawHttpClient

class KucoinHttpClient : ExchangeHttpClient() {
    private val kucoinRawHttpClient: KucoinRawHttpClient = KucoinRawHttpClient()

    override fun privateApi(accessKey: String, secretKey: String): PrivateHttpClient {
        val split = secretKey.split("/")
        return KucoinPrivateHttpClient(kucoinRawHttpClient.privateApi(accessKey, split[0], split[1]))
    }

    override fun publicApi(): PublicHttpClient {
        TODO("not implemented")
    }
}
