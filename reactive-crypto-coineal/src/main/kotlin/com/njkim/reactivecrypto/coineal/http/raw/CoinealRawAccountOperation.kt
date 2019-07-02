package com.njkim.reactivecrypto.coineal.http.raw

import com.njkim.reactivecrypto.coineal.model.CoinealApiResponse
import com.njkim.reactivecrypto.coineal.model.CoinealBalance
import com.njkim.reactivecrypto.coineal.model.CoinealBalanceWrapper
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.common.util.toMultiValueMap
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Flux
import java.time.ZonedDateTime

/**
Created by jay on 02/07/2019
 **/
class CoinealRawAccountOperation(
    private val webClient: WebClient,
    private val accessKey: String,
    private val secretKey: String
) {
    fun balance(): Flux<CoinealBalance> {
        var params = mapOf(
            "api_key" to accessKey,
            "time" to ZonedDateTime.now().toEpochMilli()
        )
        params = sign(params, secretKey)
        return webClient.get()
            .uri {
                it.path("/open/api/user/account")
                    .queryParams(params.toMultiValueMap())
                    .build()
            }
            .retrieve()
            .bodyToMono<CoinealApiResponse<CoinealBalanceWrapper>>()
            .flatMapIterable {
                it.data.coinList
            }
    }
}