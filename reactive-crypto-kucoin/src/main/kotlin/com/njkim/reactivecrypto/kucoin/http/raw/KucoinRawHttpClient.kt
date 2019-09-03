package com.njkim.reactivecrypto.kucoin.http.raw

import com.njkim.reactivecrypto.kucoin.KucoinJsonObjectMapper
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.http.codec.json.Jackson2JsonEncoder
import org.springframework.util.MimeTypeUtils
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriComponentsBuilder
import reactor.netty.http.client.HttpClient

class KucoinRawHttpClient(
    private val scheme: String = "https",
    private val host: String = "api.kucoin.com"
) {
    private val webClient: WebClient = createDefaultWebClientBuilder().build()

    fun publicApi(): KucoinRawPublicHttpClient {
        return KucoinRawPublicHttpClient(webClient)
    }

    fun privateApi(accessKey: String, secretKey: String, passphrase: String): KucoinRawPrivateHttpClient {
        return KucoinRawPrivateHttpClient(webClient, accessKey, secretKey, passphrase)
    }

    private fun createDefaultWebClientBuilder(): WebClient.Builder {
        val strategies = ExchangeStrategies.builder()
            .codecs { clientCodecConfigurer ->
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonEncoder(
                        Jackson2JsonEncoder(
                            KucoinJsonObjectMapper.instance,
                            MimeTypeUtils.APPLICATION_JSON
                        )
                    )
                clientCodecConfigurer.defaultCodecs()
                    .jackson2JsonDecoder(
                        Jackson2JsonDecoder(
                            KucoinJsonObjectMapper.instance,
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
            .baseUrl(
                UriComponentsBuilder.newInstance()
                    .scheme(scheme)
                    .host(host)
                    .build()
                    .toUriString()
            )
    }
}
