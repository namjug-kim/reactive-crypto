package com.njkim.reactivecrypto.binance

import com.njkim.reactivecrypto.core.ExchangeClientFactory
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.websocket.ExchangeWebsocketClient
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExchangeClientFactoryTest {
    @Test
    fun `create binance websocket client`() {
        val exchangeWebsocketClient = ExchangeClientFactory.websocket(ExchangeVendor.BINANCE)

        assertThat(exchangeWebsocketClient).isNotNull
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangeWebsocketClient::class.java)
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(BinanceWebsocketClient::class.java)
    }
}