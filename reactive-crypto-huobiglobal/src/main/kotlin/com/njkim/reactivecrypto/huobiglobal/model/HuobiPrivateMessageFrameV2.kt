package com.njkim.reactivecrypto.huobiglobal.model

import com.fasterxml.jackson.annotation.JsonProperty

data class HuobiPrivateMessageFrameV2<T>(
    @JsonProperty("action")
    val action: String,
    @JsonProperty("ch")
    val ch: String,
    @JsonProperty("data")
    val data: T
)
