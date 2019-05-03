package com.njkim.reactivecrypto.core.common.util

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair

object CurrencyPairUtil {
    private val currencyPairMap: MutableMap<String, CurrencyPair> = HashMap()

    init {
        Currency.values().forEach { targetCurrency ->
            Currency.FIAT_CURRENCIES.forEach { fiatCurrency ->
                val currencyPair = CurrencyPair(targetCurrency, fiatCurrency)
                currencyPairMap["${targetCurrency.name}${fiatCurrency.name}"] = currencyPair
            }
        }
    }

    fun parse(rawValue: String): CurrencyPair? {
        return currencyPairMap[rawValue.toUpperCase()]
    }
}