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

package com.njkim.reactivecrypto.bitmax

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class BitmaxRawWebsocketClientTest {
    @Test
    fun `bitmax tick data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val recentTradeMaxCount = 20
        val bitmaxTradeDataFlux = BitmaxRawWebsocketClient()
            .createTradeDataFlux(symbol, recentTradeMaxCount)

        // when
        StepVerifier.create(bitmaxTradeDataFlux.limitRequest(2))
            // then
            .assertNext { bitmaxTickDataWrapper ->
                assertThat(bitmaxTickDataWrapper.m)
                    .isEqualTo("marketTrades")
                assertThat(bitmaxTickDataWrapper.s)
                    .isEqualTo(symbol)
                assertThat(bitmaxTickDataWrapper.trades.size)
                    .withFailMessage("max number of recent trades to be included in the first market trades message")
                    .isEqualTo(recentTradeMaxCount)
                assertThat(bitmaxTickDataWrapper.trades[0].price)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(bitmaxTickDataWrapper.trades[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(bitmaxTickDataWrapper.trades[0].timestamp)
                    .isNotNull()
            }
            .assertNext { bitmaxTickDataWrapper ->
                assertThat(bitmaxTickDataWrapper.m)
                    .isEqualTo("marketTrades")
                assertThat(bitmaxTickDataWrapper.s)
                    .isEqualTo(symbol)
            }
            .verifyComplete()
    }

    @Test
    fun `bitmax orderBook data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val marketDepthLevel = 20
        val bitmaxTradeDataFlux = BitmaxRawWebsocketClient()
            .createOrderBookFlux(symbol, marketDepthLevel)

        // when
        StepVerifier.create(bitmaxTradeDataFlux.limitRequest(1))
            // then
            .assertNext { bitmaxOrderBookWrapper ->
                assertThat(bitmaxOrderBookWrapper.m)
                    .isEqualTo("depth")
                assertThat(bitmaxOrderBookWrapper.s)
                    .isEqualTo(symbol)
                assertThat(bitmaxOrderBookWrapper.asks)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty
                assertThat(bitmaxOrderBookWrapper.bids)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty

                assertThat(bitmaxOrderBookWrapper.asks.size)
                    .isEqualTo(marketDepthLevel)
                assertThat(bitmaxOrderBookWrapper.bids.size)
                    .isEqualTo(marketDepthLevel)

                Assertions.assertThat(bitmaxOrderBookWrapper.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(bitmaxOrderBookWrapper.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(bitmaxOrderBookWrapper.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(bitmaxOrderBookWrapper.bids[0].price)

                Assertions.assertThat(bitmaxOrderBookWrapper.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(bitmaxOrderBookWrapper.asks[1].price)
                Assertions.assertThat(bitmaxOrderBookWrapper.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(bitmaxOrderBookWrapper.bids[1].price)
            }
            .verifyComplete()
    }
}