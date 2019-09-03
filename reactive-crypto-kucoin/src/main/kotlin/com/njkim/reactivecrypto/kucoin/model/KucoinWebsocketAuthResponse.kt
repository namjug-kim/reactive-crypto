package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty

data class KucoinWebsocketAuthResponse(
    val instanceServers: List<KucoinInstantServer>,
    val token: String
) {
    data class KucoinInstantServer(
        @JsonProperty("encrypt")
        val encrypt: Boolean,
        @JsonProperty("endpoint")
        val endpoint: String,
        @JsonProperty("pingInterval")
        val pingInterval: Int,
        @JsonProperty("pingTimeout")
        val pingTimeout: Int,
        @JsonProperty("protocol")
        val protocol: String
    )
}