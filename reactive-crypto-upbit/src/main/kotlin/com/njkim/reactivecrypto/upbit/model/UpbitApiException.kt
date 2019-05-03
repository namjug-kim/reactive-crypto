package com.njkim.reactivecrypto.upbit.model

import org.springframework.http.HttpStatus

data class UpbitApiException(
    val httpStatus: HttpStatus,
    val upbitErrorResponse: UpbitErrorResponse
) : RuntimeException()