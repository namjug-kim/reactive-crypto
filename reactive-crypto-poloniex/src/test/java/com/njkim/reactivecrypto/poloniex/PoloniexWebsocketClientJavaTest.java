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

package com.njkim.reactivecrypto.poloniex;

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair;
import com.njkim.reactivecrypto.core.common.model.order.OrderBook;
import com.njkim.reactivecrypto.core.common.model.order.TickData;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PoloniexWebsocketClientJavaTest {
    @Test
    public void tick_data_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("XRP", "BTC");
        Flux<TickData> tickDataFlux = new PoloniexWebsocketClient()
                .createTradeWebsocket(Collections.singletonList(targetCurrencyPair));

        // when
        StepVerifier.create(tickDataFlux.limitRequest(3))
                .expectNextCount(1)
                // then
                .assertNext(tickData -> {
                    assertThat(tickData).isNotNull();
                    assertThat(tickData.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(tickData.getExchangeVendor())
                            .isEqualTo(ExchangeVendor.POLONIEX);
                    assertThat(tickData.getPrice())
                            .isGreaterThan(BigDecimal.ZERO);
                    assertThat(tickData.getQuantity())
                            .isGreaterThan(BigDecimal.ZERO);
                })
                .assertNext(tickData -> {
                    assertThat(tickData).isNotNull();
                    assertThat(tickData.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(tickData.getExchangeVendor())
                            .isEqualTo(ExchangeVendor.POLONIEX);
                    assertThat(tickData.getPrice())
                            .isGreaterThan(BigDecimal.ZERO);
                    assertThat(tickData.getQuantity())
                            .isGreaterThan(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    @Test
    public void orderBook_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("XRP", "BTC");
        Flux<OrderBook> orderBookFlux = new PoloniexWebsocketClient()
                .createDepthSnapshot(Collections.singletonList(targetCurrencyPair));

        // when
        StepVerifier.create(orderBookFlux.limitRequest(5))
                .expectNextCount(3)
                // then
                .assertNext(orderBook -> {
                    assertThat(orderBook).isNotNull();
                    assertThat(orderBook.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(orderBook.getExchangeVendor())
                            .isEqualTo(ExchangeVendor.POLONIEX);
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
                })
                .assertNext(orderBook -> {
                    assertThat(orderBook).isNotNull();
                    assertThat(orderBook.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(orderBook.getExchangeVendor())
                            .isEqualTo(ExchangeVendor.POLONIEX);
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
                })
                .verifyComplete();
    }
}
