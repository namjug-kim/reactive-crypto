package com.njkim.reactivecrypto.core.common.model

import com.njkim.reactivecrypto.core.common.model.currency.Currency.*
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CurrencyPairUtilTest {
    @Test
    fun `parse raw currencyPair string`() {
        assertThat(CurrencyPairUtil.parse("btcusdt"))
            .isEqualTo(CurrencyPair(BTC, USDT))

        assertThat(CurrencyPairUtil.parse("BTCUSDT"))
            .isEqualTo(CurrencyPair(BTC, USDT))

        assertThat(CurrencyPairUtil.parse("btckrw"))
            .isEqualTo(CurrencyPair(BTC, KRW))

        assertThat(CurrencyPairUtil.parse("BTCKRW"))
            .isEqualTo(CurrencyPair(BTC, KRW))

        assertThat(CurrencyPairUtil.parse("ethbtc"))
            .isEqualTo(CurrencyPair(ETH, BTC))

        assertThat(CurrencyPairUtil.parse("ETHBTC"))
            .isEqualTo(CurrencyPair(ETH, BTC))
    }
}