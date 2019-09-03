package com.njkim.reactivecrypto.kucoin.model

data class KucoinHttpResponse<T>(
    val code: String,
    val data: T
)
