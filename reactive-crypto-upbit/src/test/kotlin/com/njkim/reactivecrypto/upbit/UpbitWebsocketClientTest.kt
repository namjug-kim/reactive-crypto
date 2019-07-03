package com.njkim.reactivecrypto.upbit

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.math.BigDecimal
import java.time.Duration

class UpbitWebsocketClientTest {
    private val log = KotlinLogging.logger {}

    @Test
    fun `upbit tick data subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "KRW")
        val upbitWebsocketClient = UpbitWebsocketClient()
            .createTradeWebsocket(listOf(targetCurrencyPair))

        // when
        val tickData = upbitWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { tickData }

        // then
        assertThat(tickData).isNotNull
        assertThat(tickData.currencyPair)
            .isEqualTo(targetCurrencyPair)
        assertThat(tickData.exchangeVendor)
            .isEqualTo(ExchangeVendor.UPBIT)
        assertThat(tickData.price)
            .isGreaterThan(BigDecimal.ZERO)
        assertThat(tickData.quantity)
            .isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `upbit orderBook subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "KRW")
        val upbitWebsocketClient = UpbitWebsocketClient()
            .createDepthSnapshot(listOf(targetCurrencyPair))

        // when
        val orderBook = upbitWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { orderBook }

        // then
        assertThat(orderBook).isNotNull
        assertThat(orderBook.currencyPair)
            .isEqualTo(targetCurrencyPair)
        assertThat(orderBook.exchangeVendor)
            .isEqualTo(ExchangeVendor.UPBIT)
        assertThat(orderBook.asks)
            .isNotEmpty
        assertThat(orderBook.bids)
            .isNotEmpty

        assertThat(orderBook.asks[0].quantity)
            .isGreaterThan(BigDecimal.ZERO)
        assertThat(orderBook.bids[0].quantity)
            .isGreaterThan(BigDecimal.ZERO)

        assertThat(orderBook.asks[0].price)
            .withFailMessage("ask price must be bigger than bid price")
            .isGreaterThan(orderBook.bids[0].price)

        assertThat(orderBook.asks[0].price)
            .withFailMessage("asks must be sorted by price asc")
            .isLessThan(orderBook.asks[1].price)
        assertThat(orderBook.bids[0].price)
            .withFailMessage("bids must be sorted by price desc")
            .isGreaterThan(orderBook.bids[1].price)
    }
}