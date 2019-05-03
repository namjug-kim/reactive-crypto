package com.njkim.reactivecrypto.binance

import com.njkim.reactivecrypto.core.ExchangeClientFactory
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExchangeClientFactoryTest {
    @Test
    fun `create binance websocket client`() {
        val exchangeWebsocketClient = ExchangeClientFactory.getInstance(ExchangeVendor.BINANCE)

        assertThat(exchangeWebsocketClient).isNotNull
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangeWebsocketClient::class.java)
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(BinanceWebsocketClient::class.java)
    }
}