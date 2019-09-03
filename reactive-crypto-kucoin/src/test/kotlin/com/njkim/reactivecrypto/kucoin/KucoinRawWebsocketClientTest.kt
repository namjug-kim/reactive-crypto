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

package com.njkim.reactivecrypto.kucoin

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.kucoin.model.KucoinMarketLevel2
import com.njkim.reactivecrypto.kucoin.model.KucoinMatchExecutionData
import com.njkim.reactivecrypto.kucoin.model.KucoinMessageFrame
import org.assertj.core.api.Assertions
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class KucoinRawWebsocketClientTest {

    @Test
    fun `tick data subscribe`() {
        // given
        val symbol = CurrencyPair(Currency.BTC, Currency.USDT)
        val tradeDataFlux = KucoinRawWebsocketClient()
            .createMatchExecutionData(listOf(symbol))

        // when
        StepVerifier.create(tradeDataFlux.limitRequest(5))
            .expectNextCount(4)
            // then
            .assertNext { tickDataMessageFrame: KucoinMessageFrame<KucoinMatchExecutionData> ->
                Assertions.assertThat(tickDataMessageFrame.data.symbol)
                    .isEqualTo(symbol)
                Assertions.assertThat(tickDataMessageFrame.data.price)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(tickDataMessageFrame.data.size)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(tickDataMessageFrame.data.time)
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

        val level2MarketDataFlux = KucoinRawWebsocketClient()
            .createLevel2MarketDataFlux(symbols)

        // when
        StepVerifier.create(level2MarketDataFlux.limitRequest(5))
            .expectNextCount(4)
            // then
            .assertNext { orderBookMessageFrame: KucoinMessageFrame<KucoinMarketLevel2> ->
                Assertions.assertThat(orderBookMessageFrame.data.symbol)
                    .isIn(symbols)
            }
            .verifyComplete()
    }
}
