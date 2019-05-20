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

package com.njkim.reactivecrypto.idax

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.idax.model.IdaxMessageFrame
import com.njkim.reactivecrypto.idax.model.IdaxTickData
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class IdaxRawWebsocketClientTest {
    @Test
    fun `idax tick data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val bitmaxTradeDataFlux = IdaxRawWebsocketClient()
            .createTradeDataFlux(listOf(symbol))

        // when
        StepVerifier.create(bitmaxTradeDataFlux.limitRequest(2))
            // then
            .assertNext { idaxMessageFrame: IdaxMessageFrame<List<IdaxTickData>> ->
                assertThat(idaxMessageFrame.currencyPair).isEqualTo(symbol)
                assertThat(idaxMessageFrame.code).isEqualTo("00000")
            }
            .assertNext { idaxMessageFrame: IdaxMessageFrame<List<IdaxTickData>> ->
                assertThat(idaxMessageFrame.currencyPair).isEqualTo(symbol)
                assertThat(idaxMessageFrame.code).isEqualTo("00000")
                assertThat(idaxMessageFrame.data).isNotEmpty
                assertThat(idaxMessageFrame.data[0].price).isGreaterThan(BigDecimal.ZERO)
                assertThat(idaxMessageFrame.data[0].volume).isGreaterThan(BigDecimal.ZERO)
            }
            .verifyComplete()
    }

    @Test
    fun `idax orderBook change data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val bitmaxTradeDataFlux = IdaxRawWebsocketClient()
            .createOrderBookChangeFlux(listOf(symbol))

        // when
        StepVerifier.create(bitmaxTradeDataFlux.limitRequest(1))
            // then
            .assertNext { idaxMessageFrame ->
                assertThat(idaxMessageFrame.currencyPair).isEqualTo(symbol)
                assertThat(idaxMessageFrame.code).isEqualTo("00000")
                assertThat(idaxMessageFrame.data).isNotEmpty
                assertThat(idaxMessageFrame.data[0].asks)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty
                assertThat(idaxMessageFrame.data[0].bids)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(idaxMessageFrame.data[0].bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(idaxMessageFrame.data[0].bids[0].price)

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(idaxMessageFrame.data[0].asks[1].price)
                Assertions.assertThat(idaxMessageFrame.data[0].bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(idaxMessageFrame.data[0].bids[1].price)
            }
            .verifyComplete()
    }

    @Test
    fun `idax orderBook snapshot data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val depth = 5
        val bitmaxTradeDataFlux = IdaxRawWebsocketClient()
            .createOrderBookSnapShotFlux(listOf(symbol), depth)

        // when
        StepVerifier.create(bitmaxTradeDataFlux.limitRequest(1))
            // then
            .assertNext { idaxMessageFrame ->
                assertThat(idaxMessageFrame.currencyPair).isEqualTo(symbol)
                assertThat(idaxMessageFrame.code).isEqualTo("00000")
                assertThat(idaxMessageFrame.data).isNotEmpty
                assertThat(idaxMessageFrame.data[0].asks)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty
                assertThat(idaxMessageFrame.data[0].bids)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty

                assertThat(idaxMessageFrame.data[0].bids.size)
                    .isEqualTo(depth)
                assertThat(idaxMessageFrame.data[0].asks.size)
                    .isEqualTo(depth)

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(idaxMessageFrame.data[0].bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(idaxMessageFrame.data[0].bids[0].price)

                Assertions.assertThat(idaxMessageFrame.data[0].asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(idaxMessageFrame.data[0].asks[1].price)
                Assertions.assertThat(idaxMessageFrame.data[0].bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(idaxMessageFrame.data[0].bids[1].price)
            }
            .verifyComplete()
    }
}