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

package com.njkim.reactivecrypto.poloniex

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.poloniex.model.PoloniexEventType
import com.njkim.reactivecrypto.poloniex.model.PoloniexOrderBookSnapshotEvent
import com.njkim.reactivecrypto.poloniex.model.PoloniexOrderBookUpdateEvent
import mu.KotlinLogging
import org.assertj.core.api.Assertions
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class PoloniexRawWebsocketClientTest {
    val log = KotlinLogging.logger { }

    @Test
    fun `priceAggregatedBook orderBook snapshot`() {
        val targetCurrencyPair = CurrencyPair(Currency.BTC, Currency.USDT)
        val priceAggregatedBook = PoloniexRawWebsocketClient().priceAggregatedBook(listOf(targetCurrencyPair))
            .doOnNext { log.debug { it } }

        StepVerifier.create(priceAggregatedBook.next()) // first response is orderBookSnapShot('i')
            .assertNext {
                val orderBookSnapshotEvent = it.events[0] as PoloniexOrderBookSnapshotEvent
                Assertions.assertThat(orderBookSnapshotEvent).isNotNull
                Assertions.assertThat(orderBookSnapshotEvent.eventType)
                    .isEqualTo(PoloniexEventType.ORDER_BOOK_SNAPSHOT)
                Assertions.assertThat(orderBookSnapshotEvent.bids)
                    .isNotEmpty
                Assertions.assertThat(orderBookSnapshotEvent.asks)
                    .isNotEmpty

                Assertions.assertThat(orderBookSnapshotEvent.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(orderBookSnapshotEvent.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(orderBookSnapshotEvent.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(orderBookSnapshotEvent.bids[0].price)

                Assertions.assertThat(orderBookSnapshotEvent.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(orderBookSnapshotEvent.asks[1].price)
                Assertions.assertThat(orderBookSnapshotEvent.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(orderBookSnapshotEvent.bids[1].price)
            }
            .verifyComplete()
    }

    @Test
    fun `priceAggregatedBook orderBook update`() {
        val targetCurrencyPair = CurrencyPair(Currency.BTC, Currency.USDT)
        val priceAggregatedBook = PoloniexRawWebsocketClient().priceAggregatedBook(listOf(targetCurrencyPair))
            .flatMapIterable { it.events }
            .filter { it.eventType == PoloniexEventType.ORDER_BOOK_UPDATE }
            .map { it as PoloniexOrderBookUpdateEvent }
            .doOnNext { log.debug { it } }

        StepVerifier.create(priceAggregatedBook.next())
            .assertNext { orderBookUpdateEvent ->
                Assertions.assertThat(orderBookUpdateEvent).isNotNull
                Assertions.assertThat(orderBookUpdateEvent.eventType)
                    .isEqualTo(PoloniexEventType.ORDER_BOOK_UPDATE)
                Assertions.assertThat(orderBookUpdateEvent.currencyPair)
                    .isEqualTo(targetCurrencyPair)
            }
            .verifyComplete()
    }
}
