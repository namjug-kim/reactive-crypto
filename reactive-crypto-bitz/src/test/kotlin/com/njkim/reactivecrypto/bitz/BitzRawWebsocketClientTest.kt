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

package com.njkim.reactivecrypto.bitz

import com.njkim.reactivecrypto.bitz.model.BitzMessageFrame
import com.njkim.reactivecrypto.bitz.model.BitzOrderBook
import com.njkim.reactivecrypto.bitz.model.BitzTradeData
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import org.assertj.core.api.Assertions
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class BitzRawWebsocketClientTest {
    @Test
    fun `tick data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val tradeDataFlux = BitzRawWebsocketClient()
            .createTradeFlux(listOf(symbol))

        // when
        StepVerifier.create(tradeDataFlux.limitRequest(5))
            .expectNextCount(4)
            // then
            .assertNext { tickDataMessageFrame: BitzMessageFrame<List<BitzTradeData>> ->
                Assertions.assertThat(tickDataMessageFrame.params.symbol)
                    .isEqualTo(symbol)
                Assertions.assertThat(tickDataMessageFrame.data[0].price)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(tickDataMessageFrame.data[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(tickDataMessageFrame.data[0].timestamp)
                    .isNotNull()
            }
            .verifyComplete()
    }

    @Test
    fun `orderBook change data subscribe`() {
        // given
        val symbols = listOf(
            CurrencyPair(Currency.BTC, Currency.USDT),
            CurrencyPair(Currency.ETH, Currency.USDT)
        )
        val orderBookFlux = BitzRawWebsocketClient()
            .createDepthFlux(symbols)

        // when
        StepVerifier.create(orderBookFlux.limitRequest(1))
            // then
            .assertNext { orderBookMessageFrame: BitzMessageFrame<BitzOrderBook> ->
                val orderBook = orderBookMessageFrame.data
                Assertions.assertThat(orderBook.asks)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty
                Assertions.assertThat(orderBook.bids)
                    .withFailMessage("first message shows the current orderBook snapshot.")
                    .isNotEmpty

                Assertions.assertThat(orderBook.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(orderBook.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(orderBook.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThanOrEqualTo(orderBook.bids[0].price)

                Assertions.assertThat(orderBook.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(orderBook.asks[1].price)
                Assertions.assertThat(orderBook.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(orderBook.bids[1].price)
            }
            .verifyComplete()
    }
}
