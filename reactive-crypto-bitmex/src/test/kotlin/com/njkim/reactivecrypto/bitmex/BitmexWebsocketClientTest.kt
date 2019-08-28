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

package com.njkim.reactivecrypto.bitmex

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import org.assertj.core.api.Assertions
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class BitmexWebsocketClientTest {
    private val log = KotlinLogging.logger {}

    @Test
    fun `bitmex tick data subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("XBT", "USD")
        val tickDataFlux = BitmexWebsocketClient()
            .createTradeWebsocket(listOf(targetCurrencyPair))

        // when
        StepVerifier.create(tickDataFlux.limitRequest(5))
            .expectNextCount(3)
            // then
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.exchangeVendor)
                    .isEqualTo(ExchangeVendor.BITMEX)
                Assertions.assertThat(it.price)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.quantity)
                    .isGreaterThan(BigDecimal.ZERO)
            }
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.exchangeVendor)
                    .isEqualTo(ExchangeVendor.BITMEX)
                Assertions.assertThat(it.price)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.quantity)
                    .isGreaterThan(BigDecimal.ZERO)
            }
            .verifyComplete()
    }

    @Test
    fun `bitmex orderBook subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("XBT", "USD")
        val orderBookFlux = BitmexWebsocketClient()
            .createDepthSnapshot(listOf(targetCurrencyPair))
            .doOnNext { log.info { it } }

        // when
        StepVerifier.create(orderBookFlux.limitRequest(5))
            .expectNextCount(3)
            // then
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.exchangeVendor)
                    .isEqualTo(ExchangeVendor.BITMEX)
                Assertions.assertThat(it.asks)
                    .isNotEmpty
                Assertions.assertThat(it.bids)
                    .isNotEmpty

                Assertions.assertThat(it.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(it.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(it.bids[0].price)

                Assertions.assertThat(it.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(it.asks[1].price)
                Assertions.assertThat(it.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(it.bids[1].price)
            }
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.exchangeVendor)
                    .isEqualTo(ExchangeVendor.BITMEX)
                Assertions.assertThat(it.asks)
                    .isNotEmpty
                Assertions.assertThat(it.bids)
                    .isNotEmpty

                Assertions.assertThat(it.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(it.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(it.bids[0].price)

                Assertions.assertThat(it.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(it.asks[1].price)
                Assertions.assertThat(it.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(it.bids[1].price)
            }
            .verifyComplete()
    }
}
