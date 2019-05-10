package com.njkim.reactivecrypto.binance

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import org.assertj.core.api.Assertions
import org.junit.Test
import java.math.BigDecimal
import java.time.Duration

class BinanceWebsocketClientTest {
    private val log = KotlinLogging.logger {}

    @Test
    fun `binance tick data subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "USDT")
        val binanceWebsocketClient = BinanceWebsocketClient()
            .createTradeWebsocket(listOf(targetCurrencyPair))

        // when
        val tickData = binanceWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { tickData }

        // then
        Assertions.assertThat(tickData).isNotNull
        Assertions.assertThat(tickData.currencyPair)
            .isEqualTo(targetCurrencyPair)
        Assertions.assertThat(tickData.exchangeVendor)
            .isEqualByComparingTo(ExchangeVendor.BINANCE)
        Assertions.assertThat(tickData.price)
            .isGreaterThan(BigDecimal.ZERO)
        Assertions.assertThat(tickData.quantity)
            .isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `binance orderBook subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "USDT")
        val binanceWebsocketClient = BinanceWebsocketClient()
            .createDepthSnapshot(listOf(targetCurrencyPair))

        // when
        val orderBook = binanceWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { orderBook }

        // then
        Assertions.assertThat(orderBook).isNotNull
        Assertions.assertThat(orderBook.currencyPair)
            .isEqualTo(targetCurrencyPair)
        Assertions.assertThat(orderBook.exchangeVendor)
            .isEqualByComparingTo(ExchangeVendor.BINANCE)
        Assertions.assertThat(orderBook.asks)
            .isNotEmpty
        Assertions.assertThat(orderBook.bids)
            .isNotEmpty

        Assertions.assertThat(orderBook.asks[0].quantity)
            .isGreaterThan(BigDecimal.ZERO)
        Assertions.assertThat(orderBook.bids[0].quantity)
            .isGreaterThan(BigDecimal.ZERO)

        Assertions.assertThat(orderBook.asks[0].price)
            .withFailMessage("ask price must be bigger than bid price")
            .isGreaterThan(orderBook.bids[0].price)

        Assertions.assertThat(orderBook.asks[0].price)
            .withFailMessage("asks must be sorted by price asc")
            .isLessThan(orderBook.asks[1].price)
        Assertions.assertThat(orderBook.bids[0].price)
            .withFailMessage("bids must be sorted by price desc")
            .isGreaterThan(orderBook.bids[1].price)
    }
}