package com.njkim.reactivecrypto.poloniex.model

data class PoloniexMessageFrame(
    val channelId: Long,
    val sequenceNumber: Long,
    val events: List<PoloniexEvent>
)
