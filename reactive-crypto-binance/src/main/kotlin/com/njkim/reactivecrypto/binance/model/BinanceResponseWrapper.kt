package com.njkim.reactivecrypto.binance.model

import com.njkim.reactivecrypto.binance.BinanceCommonUtil.parseCurrencyPair
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import org.apache.commons.lang3.StringUtils

data class BinanceResponseWrapper<T>(
    val stream: String,
    val data: T
) {
    fun getCurrencyPair(): CurrencyPair {
        val rawValue = StringUtils.split(stream, "@")[0]
        return parseCurrencyPair(rawValue)
    }
}