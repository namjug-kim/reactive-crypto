package com.njkim.reactivecrypto.hubi;

import com.njkim.reactivecrypto.core.ExchangeClientFactory;
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient;
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeClientFactoryJavaTest {
    @Test
    public void create_hubi_websocket_client() {
        ExchangePublicWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.publicWebsocket(ExchangeVendor.HUBI);

        assertThat(exchangeWebsocketClient).isNotNull();
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangePublicWebsocketClient.class);
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(HubiWebsocketClient.class);
    }
}
