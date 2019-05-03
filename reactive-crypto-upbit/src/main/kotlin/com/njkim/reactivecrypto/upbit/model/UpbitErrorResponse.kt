package com.njkim.reactivecrypto.upbit.model

data class UpbitErrorResponse(val error: InnerUpbitErrorResponse) {
    data class InnerUpbitErrorResponse(
        val message: String,
        val name: String
    )
}