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

package com.njkim.reactivecrypto.coineal

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.coineal.model.CoinealMessageFrame
import com.njkim.reactivecrypto.coineal.model.CoinealOrderBook
import com.njkim.reactivecrypto.coineal.model.CoinealTickDataWrapper
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class CoinealRawWebsocketClientTest {
    @Test
    fun `tick data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val tradeDataFlux = CoinealRawWebsocketClient()
            .createTradeDataFlux(listOf(symbol))

        // when
        StepVerifier.create(tradeDataFlux.limitRequest(2))
            // then
            .assertNext { coinealTickDataMessageWrapper: CoinealMessageFrame<CoinealTickDataWrapper> ->
                assertThat(coinealTickDataMessageWrapper.currencyPair).isEqualTo(symbol)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].price)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].vol)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].amount)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].ts)
                    .isNotNull()
            }
            .assertNext { coinealTickDataMessageWrapper: CoinealMessageFrame<CoinealTickDataWrapper> ->
                assertThat(coinealTickDataMessageWrapper.currencyPair).isEqualTo(symbol)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].price)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].vol)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].amount)
                    .isGreaterThan(BigDecimal.ZERO)
                assertThat(coinealTickDataMessageWrapper.tick.data[0].ts)
                    .isNotNull()
            }
            .verifyComplete()
    }

    @Test
    fun `orderBook change data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val orderBookFlux = CoinealRawWebsocketClient()
            .createOrderBookFlux(listOf(symbol), "step0")

        // when
        StepVerifier.create(orderBookFlux.limitRequest(1))
            // then
            .assertNext { coinealOrderBookMessageFrame: CoinealMessageFrame<CoinealOrderBook> ->
                assertThat(coinealOrderBookMessageFrame.tick.asks)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty
                assertThat(coinealOrderBookMessageFrame.tick.bids)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty

                Assertions.assertThat(coinealOrderBookMessageFrame.tick.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(coinealOrderBookMessageFrame.tick.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(coinealOrderBookMessageFrame.tick.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(coinealOrderBookMessageFrame.tick.bids[0].price)

                Assertions.assertThat(coinealOrderBookMessageFrame.tick.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(coinealOrderBookMessageFrame.tick.asks[1].price)
                Assertions.assertThat(coinealOrderBookMessageFrame.tick.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(coinealOrderBookMessageFrame.tick.bids[1].price)
            }
            .verifyComplete()
    }
}