package com.njkim.reactivecrypto.binance

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil

object BinanceCommonUtil {
    fun parseCurrencyPair(rawValue: String): CurrencyPair {
        val parse = CurrencyPairUtil.parse(rawValue)
        return checkNotNull(parse)
    }
}