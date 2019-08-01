package com.njkim.reactivecrypto.core.common.model

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrencyPairUtilTest {
    @Test
    fun `parse raw currencyPair string`() {
        assertThat(CurrencyPairUtil.parse("btcusdt"))
            .isEqualTo(CurrencyPair(Currency.BTC, Currency.USDT))

        assertThat(CurrencyPairUtil.parse("BTCUSDT"))
            .isEqualTo(CurrencyPair(Currency.BTC, Currency.USDT))

        assertThat(CurrencyPairUtil.parse("btckrw"))
            .isEqualTo(CurrencyPair(Currency.BTC, Currency.KRW))

        assertThat(CurrencyPairUtil.parse("BTCKRW"))
            .isEqualTo(CurrencyPair(Currency.BTC, Currency.KRW))

        assertThat(CurrencyPairUtil.parse("ethbtc"))
            .isEqualTo(CurrencyPair(Currency.ETH, Currency.BTC))

        assertThat(CurrencyPairUtil.parse("ETHBTC"))
            .isEqualTo(CurrencyPair(Currency.ETH, Currency.BTC))

        assertThat(CurrencyPairUtil.parse("UNKCURRENCYBTC"))
            .isEqualTo(CurrencyPair(Currency.getInstance("UNKCURRENCY"), Currency.BTC))
    }
}