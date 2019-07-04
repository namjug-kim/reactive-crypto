package com.njkim.reactivecrypto.binance.http.raw

import com.njkim.reactivecrypto.binance.model.BinanceApiException
import com.njkim.reactivecrypto.binance.model.BinanceErrorResponse
import org.springframework.web.reactive.function.client.WebClient

/**
Created by jay on 2019-07-04
 **/
fun WebClient.ResponseSpec.binanceErrorHandling(): WebClient.ResponseSpec = onStatus(
    { it.isError },
    { clientResponse ->
        clientResponse.bodyToMono(BinanceErrorResponse::class.java)
            .map { BinanceApiException(clientResponse.statusCode(), it) }
    })
