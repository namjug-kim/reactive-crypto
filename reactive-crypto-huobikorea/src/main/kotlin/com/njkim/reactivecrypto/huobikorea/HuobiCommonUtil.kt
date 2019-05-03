package com.njkim.reactivecrypto.huobikorea

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil

object HuobiCommonUtil {
    fun parseCurrencyPair(rawValue: String): CurrencyPair {
        val parse = CurrencyPairUtil.parse(rawValue)
        return checkNotNull(parse)
    }
}