package com.njkim.reactivecrypto.hubi;

import com.njkim.reactivecrypto.core.ExchangeClientFactory;
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient;
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeClientFactoryJavaTest {
    @Test
    public void create_binance_websocket_client() {
        ExchangeWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.getInstance(ExchangeVendor.HUBI);

        assertThat(exchangeWebsocketClient).isNotNull();
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangeWebsocketClient.class);
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(HubiWebsocketClient.class);
    }
}
