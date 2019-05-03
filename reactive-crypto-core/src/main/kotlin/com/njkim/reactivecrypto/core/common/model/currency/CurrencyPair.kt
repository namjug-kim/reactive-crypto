package com.njkim.reactivecrypto.core.common.model.currency

data class CurrencyPair(
    val targetCurrency: Currency,
    val baseCurrency: Currency
) {
    companion object {
        @JvmStatic
        fun parse(targetCurrency: String, baseCurrency: String): CurrencyPair {
            return CurrencyPair(
                Currency.valueOf(targetCurrency.toUpperCase()),
                Currency.valueOf(baseCurrency.toUpperCase())
            )
        }
    }
}