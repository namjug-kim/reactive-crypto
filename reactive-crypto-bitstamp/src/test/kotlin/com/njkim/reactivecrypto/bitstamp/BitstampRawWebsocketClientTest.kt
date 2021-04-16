package com.njkim.reactivecrypto.bitstamp

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import org.assertj.core.api.Assertions
import org.junit.Test
import reactor.test.StepVerifier
import java.math.BigDecimal

class BitstampRawWebsocketClientTest {

    @Test
    fun `live trade test`() {
        val targetCurrencyPair = CurrencyPair(Currency.BTC, Currency.USD)
        val liveTickerFlux = BitstampRawWebsocketClient().liveTicker(listOf(targetCurrencyPair))

        StepVerifier.create(liveTickerFlux.limitRequest(3))
            .expectNextCount(2)
            .assertNext { messageFrame ->
                Assertions.assertThat(messageFrame.currencyPair)
                    .isEqualTo(targetCurrencyPair)

                Assertions.assertThat(messageFrame.data.amount)
                    .isNotNull()

                Assertions.assertThat(messageFrame.data.price)
                    .isNotNull()
            }
            .verifyComplete()
    }

    @Test
    fun `live orderBook test`() {
        val targetCurrencyPair = CurrencyPair(Currency.BTC, Currency.USD)
        val orderBookFlux = BitstampRawWebsocketClient().liveOrderBook(listOf(targetCurrencyPair))

        StepVerifier.create(orderBookFlux.limitRequest(3))
            .expectNextCount(2)
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.data.asks)
                    .isNotEmpty
                Assertions.assertThat(it.data.bids)
                    .isNotEmpty

                Assertions.assertThat(it.data.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.data.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(it.data.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(it.data.bids[0].price)

                Assertions.assertThat(it.data.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(it.data.asks[1].price)
                Assertions.assertThat(it.data.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(it.data.bids[1].price)
            }
    }

    @Test
    fun `live detail orderBook test`() {
        val targetCurrencyPair = CurrencyPair(Currency.BTC, Currency.USD)
        val orderBookFlux = BitstampRawWebsocketClient().liveDetailOrderBook(listOf(targetCurrencyPair))

        StepVerifier.create(orderBookFlux.limitRequest(3))
            .expectNextCount(2)
            .assertNext {
                Assertions.assertThat(it).isNotNull
                Assertions.assertThat(it.currencyPair)
                    .isEqualTo(targetCurrencyPair)
                Assertions.assertThat(it.data.asks)
                    .isNotEmpty
                Assertions.assertThat(it.data.bids)
                    .isNotEmpty

                Assertions.assertThat(it.data.asks[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)
                Assertions.assertThat(it.data.bids[0].quantity)
                    .isGreaterThan(BigDecimal.ZERO)

                Assertions.assertThat(it.data.asks[0].orderId)
                    .isNotNull()
                Assertions.assertThat(it.data.bids[0].orderId)
                    .isNotNull()

                Assertions.assertThat(it.data.asks[0].price)
                    .withFailMessage("ask price must be bigger than bid price")
                    .isGreaterThan(it.data.bids[0].price)

                Assertions.assertThat(it.data.asks[0].price)
                    .withFailMessage("asks must be sorted by price asc")
                    .isLessThan(it.data.asks[1].price)
                Assertions.assertThat(it.data.bids[0].price)
                    .withFailMessage("bids must be sorted by price desc")
                    .isGreaterThan(it.data.bids[1].price)
            }
    }
}
