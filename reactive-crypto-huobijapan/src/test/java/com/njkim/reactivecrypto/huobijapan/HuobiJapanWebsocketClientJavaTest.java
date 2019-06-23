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

package com.njkim.reactivecrypto.huobijapan;

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair;
import com.njkim.reactivecrypto.core.common.model.order.OrderBook;
import com.njkim.reactivecrypto.core.common.model.order.TickData;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HuobiJapanWebsocketClientJavaTest {
    @Test
    public void huobi_tick_data_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "JPY");
        Flux<TickData> tickDataFlux = new HuobiJapanWebsocketClient()
                .createTradeWebsocket(Collections.singletonList(targetCurrencyPair));

        // when
        TickData tickData = tickDataFlux.blockFirst(Duration.ofSeconds(10));

        // then
        assertThat(tickData).isNotNull();
        assertThat(tickData.getCurrencyPair())
                .isEqualTo(targetCurrencyPair);
        assertThat(tickData.getExchangeVendor())
                .isEqualByComparingTo(ExchangeVendor.HUOBI_JAPAN);
        assertThat(tickData.getPrice())
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(tickData.getQuantity())
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    public void huobi_orderBook_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "JPY");
        Flux<OrderBook> orderBookFlux = new HuobiJapanWebsocketClient()
                .createDepthSnapshot(Collections.singletonList(targetCurrencyPair));

        // when
        OrderBook orderBook = orderBookFlux.blockFirst(Duration.ofSeconds(10));

        // then
        assertThat(orderBook).isNotNull();
        assertThat(orderBook.getCurrencyPair())
                .isEqualTo(targetCurrencyPair);
        assertThat(orderBook.getExchangeVendor())
                .isEqualByComparingTo(ExchangeVendor.HUOBI_JAPAN);
        assertThat(orderBook.getAsks())
                .isNotEmpty();
        assertThat(orderBook.getBids())
                .isNotEmpty();

        assertThat(orderBook.getAsks().get(0).getQuantity())
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(orderBook.getBids().get(0).getQuantity())
                .isGreaterThan(BigDecimal.ZERO);

        assertThat(orderBook.getAsks().get(0).getPrice())
                .withFailMessage("ask price must be bigger than bid price")
                .isGreaterThan(orderBook.getBids().get(0).getPrice());

        assertThat(orderBook.getAsks().get(0).getPrice())
                .withFailMessage("asks must be sorted by price asc")
                .isLessThan(orderBook.getAsks().get(1).getPrice());
        assertThat(orderBook.getBids().get(0).getPrice())
                .withFailMessage("bids must be sorted by price desc")
                .isGreaterThan(orderBook.getBids().get(1).getPrice());

    }
}
