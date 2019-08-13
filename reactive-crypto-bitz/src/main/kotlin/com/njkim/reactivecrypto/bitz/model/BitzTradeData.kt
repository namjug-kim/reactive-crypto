package com.njkim.reactivecrypto.bitz.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class BitzTradeData(
    @JsonProperty("id")
    val id: Long,

    @JsonProperty("n")
    val quantity: BigDecimal,

    @JsonProperty("p")
    val price: BigDecimal,

    @JsonProperty("s")
    val side: TradeSideType,

    /**
     *  "t": "21:04:10", #time
     */
    @JsonProperty("t")
    val timeString: String,

    /**
     * "T": 1562159050, #time stamp
     */
    @JsonProperty("T")
    val timestamp: ZonedDateTime
)