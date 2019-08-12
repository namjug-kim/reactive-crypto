package com.njkim.reactivecrypto.bhex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BhexTickData(
    /**
     * true is buy, false is sell
     */
    @JsonProperty("m")
    val m: Boolean,

    @JsonProperty("p")
    val price: BigDecimal,

    @JsonProperty("q")
    val quantity: BigDecimal,

    @JsonProperty("t")
    val time: ZonedDateTime,

    @JsonProperty("v")
    val version: String
) {
    val side: TradeSideType
        get() {
            return when {
                m -> TradeSideType.BUY
                else -> TradeSideType.SELL
            }
        }
}