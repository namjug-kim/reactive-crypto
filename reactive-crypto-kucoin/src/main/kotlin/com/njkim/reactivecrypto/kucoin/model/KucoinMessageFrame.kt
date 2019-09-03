package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty

data class KucoinMessageFrame<T>(
    @JsonProperty("data")
    val data: T,
    @JsonProperty("subject")
    val subject: String,
    @JsonProperty("topic")
    val topic: String,
    @JsonProperty("type")
    val type: String
)
