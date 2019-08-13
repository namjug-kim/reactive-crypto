package com.njkim.reactivecrypto.bitz.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.time.ZonedDateTime

data class BitzMessageFrame<T>(
    val msgId: Long,
    val action: String,
    val time: ZonedDateTime,
    val source: String,
    val params: Params,
    val data: T
) {
    data class Params(
        val symbol: CurrencyPair
    )
}