package com.njkim.reactivecrypto.huobikorea;

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair;
import com.njkim.reactivecrypto.core.common.model.order.OrderBook;
import com.njkim.reactivecrypto.core.common.model.order.TickData;
import org.junit.Test;
import reactor.core.publisher.Flux;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HuobiWebsocketClientJavaTest {
    @Test
    public void huobi_tick_data_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "KRW");
        Flux<TickData> tickDataFlux = new HuobiWebsocketClient()
                .createTradeWebsocket(Collections.singletonList(targetCurrencyPair));

        // when
        TickData tickData = tickDataFlux.blockFirst(Duration.ofSeconds(10));

        // then
        assertThat(tickData).isNotNull();
        assertThat(tickData.getCurrencyPair())
                .isEqualTo(targetCurrencyPair);
        assertThat(tickData.getExchangeVendor())
                .isEqualByComparingTo(ExchangeVendor.HUOBI_KOREA);
        assertThat(tickData.getPrice())
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(tickData.getQuantity())
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    public void huobi_orderBook_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "KRW");
        Flux<OrderBook> orderBookFlux = new HuobiWebsocketClient()
                .createDepthSnapshot(Collections.singletonList(targetCurrencyPair));

        // when
        OrderBook orderBook = orderBookFlux.blockFirst(Duration.ofSeconds(10));

        // then
        assertThat(orderBook).isNotNull();
        assertThat(orderBook.getCurrencyPair())
                .isEqualTo(targetCurrencyPair);
        assertThat(orderBook.getExchangeVendor())
                .isEqualByComparingTo(ExchangeVendor.HUOBI_KOREA);
        assertThat(orderBook.getAsks())
                .isNotEmpty();
        assertThat(orderBook.getBids())
                .isNotEmpty();

        assertThat(orderBook.getAsks().get(0).getQuantity())
                .isGreaterThan(BigDecimal.ZERO);
        assertThat(orderBook.getBids().get(0).getQuantity())
                .isGreaterThan(BigDecimal.ZERO);

        assertThat(orderBook.getAsks().get(0).getPrice())
                .withFailMessage("ask price must be bigger than bid price")
                .isGreaterThan(orderBook.getBids().get(0).getPrice());

        assertThat(orderBook.getAsks().get(0).getPrice())
                .withFailMessage("asks must be sorted by price asc")
                .isLessThan(orderBook.getAsks().get(1).getPrice());
        assertThat(orderBook.getBids().get(0).getPrice())
                .withFailMessage("bids must be sorted by price desc")
                .isGreaterThan(orderBook.getBids().get(1).getPrice());

    }
}