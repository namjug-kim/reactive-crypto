package com.njkim.reactivecrypto.huobikorea

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import org.assertj.core.api.Assertions
import org.junit.Test
import java.math.BigDecimal
import java.time.Duration

class HuobiKoreaWebsocketClientTest {
    private val log = KotlinLogging.logger {}

    @Test
    fun `huobi tick data subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "USDT")
        val huobiWebsocketClient = HuobiKoreaWebsocketClient()
            .createTradeWebsocket(listOf(targetCurrencyPair))

        // when
        val tickData = huobiWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { tickData }

        // then
        Assertions.assertThat(tickData).isNotNull
        Assertions.assertThat(tickData.currencyPair)
            .isEqualTo(targetCurrencyPair)
        Assertions.assertThat(tickData.exchangeVendor)
            .isEqualByComparingTo(ExchangeVendor.HUOBI_KOREA)
        Assertions.assertThat(tickData.price)
            .isGreaterThan(BigDecimal.ZERO)
        Assertions.assertThat(tickData.quantity)
            .isGreaterThan(BigDecimal.ZERO)
    }

    @Test
    fun `huobi orderBook subscribe`() {
        // given
        val targetCurrencyPair = CurrencyPair.parse("BTC", "USDT")
        val huobiWebsocketClient = HuobiKoreaWebsocketClient()
            .createDepthSnapshot(listOf(targetCurrencyPair))

        // when
        val orderBook = huobiWebsocketClient.blockFirst(Duration.ofSeconds(10))!!
        log.info { orderBook }

        // then
        Assertions.assertThat(orderBook).isNotNull
        Assertions.assertThat(orderBook.currencyPair)
            .isEqualTo(targetCurrencyPair)
        Assertions.assertThat(orderBook.exchangeVendor)
            .isEqualByComparingTo(ExchangeVendor.HUOBI_KOREA)
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