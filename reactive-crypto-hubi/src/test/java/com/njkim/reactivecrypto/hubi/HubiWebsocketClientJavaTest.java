package com.njkim.reactivecrypto.hubi;

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor;
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair;
import com.njkim.reactivecrypto.core.common.model.order.OrderBook;
import com.njkim.reactivecrypto.core.common.model.order.TickData;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class HubiWebsocketClientJavaTest {
    @Test
    public void hubi_tick_data_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "USDT");
        Flux<TickData> tickDataFlux = new HubiWebsocketClient()
                .createTradeWebsocket(Collections.singletonList(targetCurrencyPair));

        // when
        StepVerifier.create(tickDataFlux.limitRequest(5))
                .expectNextCount(3)
                // then
                .assertNext(tickData -> {
                    assertThat(tickData).isNotNull();
                    assertThat(tickData.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(tickData.getExchangeVendor())
                            .isEqualByComparingTo(ExchangeVendor.HUBI);
                    assertThat(tickData.getPrice())
                            .isGreaterThan(BigDecimal.ZERO);
                    assertThat(tickData.getQuantity())
                            .isGreaterThan(BigDecimal.ZERO);
                })
                .assertNext(tickData -> {
                    assertThat(tickData).isNotNull();
                    assertThat(tickData.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(tickData.getExchangeVendor())
                            .isEqualByComparingTo(ExchangeVendor.HUBI);
                    assertThat(tickData.getPrice())
                            .isGreaterThan(BigDecimal.ZERO);
                    assertThat(tickData.getQuantity())
                            .isGreaterThan(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    @Test
    public void hubi_orderBook_subscribe() {
        // given
        CurrencyPair targetCurrencyPair = CurrencyPair.parse("BTC", "USDT");
        Flux<OrderBook> orderBookFlux = new HubiWebsocketClient()
                .createDepthSnapshot(Collections.singletonList(targetCurrencyPair));

        // when
        StepVerifier.create(orderBookFlux.limitRequest(5))
                .expectNextCount(3)
                // then
                .assertNext(orderBook -> {
                    assertThat(orderBook).isNotNull();
                    assertThat(orderBook.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(orderBook.getExchangeVendor())
                            .isEqualByComparingTo(ExchangeVendor.HUBI);
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
                })
                .assertNext(orderBook -> {
                    assertThat(orderBook).isNotNull();
                    assertThat(orderBook.getCurrencyPair())
                            .isEqualTo(targetCurrencyPair);
                    assertThat(orderBook.getExchangeVendor())
                            .isEqualByComparingTo(ExchangeVendor.HUBI);
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
                })
                .verifyComplete();
    }
}