package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import java.math.BigDecimal

data class KucoinAccount(
    @JsonProperty("available")
    val available: BigDecimal,
    @JsonProperty("balance")
    val balance: BigDecimal,
    @JsonProperty("currency")
    val currency: Currency,
    @JsonProperty("holds")
    val holds: BigDecimal,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("type")
    val type: KucoinAccountType
) {
    enum class KucoinAccountType {
        MAIN,
        TRADE
    }
}
