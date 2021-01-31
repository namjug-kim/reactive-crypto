package com.njkim.reactivecrypto.okex

import com.njkim.reactivecrypto.core.ExchangeClientFactory
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExchangeClientFactoryTest {
    @Test
    fun `create okex websocket client`() {
        val exchangeWebsocketClient = ExchangeClientFactory.publicWebsocket(ExchangeVendor.OKEX)

        assertThat(exchangeWebsocketClient).isNotNull
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangePublicWebsocketClient::class.java)
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(OkexWebsocketClient::class.java)
    }
}
