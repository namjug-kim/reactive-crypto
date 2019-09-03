package com.njkim.reactivecrypto.kucoin.model

data class KucoinResponseBody<T>(
    val code: String,
    val data: T
)