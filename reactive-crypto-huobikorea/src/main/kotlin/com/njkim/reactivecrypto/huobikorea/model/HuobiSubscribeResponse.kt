package com.njkim.reactivecrypto.huobikorea.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.huobikorea.HuobiCommonUtil
import java.time.ZonedDateTime
import java.util.regex.Pattern

data class HuobiSubscribeResponse<T>(
    val ch: String,
    val ts: ZonedDateTime,
    val tick: T
) {
    val currencyPair: CurrencyPair
        get() {
            val pattern = Pattern.compile("(market.)([a-z]+)(.*)", Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(ch)
            if (matcher.matches()) {
                val group = matcher.group(2)
                return HuobiCommonUtil.parseCurrencyPair(group)
            }

            throw IllegalArgumentException()
        }
}