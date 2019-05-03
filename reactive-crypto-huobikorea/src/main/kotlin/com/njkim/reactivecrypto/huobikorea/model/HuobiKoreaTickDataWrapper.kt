package com.njkim.reactivecrypto.huobikorea.model

import java.math.BigDecimal
import java.time.ZonedDateTime

data class HuobiKoreaTickDataWrapper(
    val id: BigDecimal,
    val ts: ZonedDateTime,
    val data: List<HuobiKoreaTickData>
)

data class HuobiKoreaTickData(
    val id: BigDecimal,
    val amount: BigDecimal,
    val ts: ZonedDateTime,
    val price: BigDecimal,
    val direction: String
)