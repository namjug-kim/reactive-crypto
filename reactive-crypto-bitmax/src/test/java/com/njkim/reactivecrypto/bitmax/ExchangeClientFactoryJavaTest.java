/*
 * Copyright 2019 namjug-kim
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.njkim.reactivecrypto.bitmax;

import com.njkim.reactivecrypto.core.ExchangeClientFactory;
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import com.njkim.reactivecrypto.core.websocket.ExchangePublicWebsocketClient;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ExchangeClientFactoryJavaTest {
    @Test
    public void bitmax() {
        ExchangePublicWebsocketClient exchangeWebsocketClient = ExchangeClientFactory.publicWebsocket(ExchangeVendor.BITMAX);

        assertThat(exchangeWebsocketClient).isNotNull();
        assertThat(exchangeWebsocketClient).isInstanceOf(ExchangePublicWebsocketClient.class);
        assertThat(exchangeWebsocketClient).isExactlyInstanceOf(BitmaxWebsocketClient.class);
    }
}
