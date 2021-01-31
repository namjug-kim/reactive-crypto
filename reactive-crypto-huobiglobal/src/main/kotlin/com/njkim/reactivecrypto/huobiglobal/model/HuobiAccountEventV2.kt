package com.njkim.reactivecrypto.huobiglobal.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.time.ZonedDateTime

data class HuobiAccountEventV2(
    val symbol: CurrencyPair,
    val accountId: String,
    val available: String,
    val changeType: String,
    val accountType: String,
    val changeTime: ZonedDateTime
)
