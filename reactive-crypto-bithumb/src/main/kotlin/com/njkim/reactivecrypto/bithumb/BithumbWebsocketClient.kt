package com.njkim.reactivecrypto.bithumb

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.bithumb.model.BithumbOrderBook
import com.njkim.reactivecrypto.bithumb.model.BithumbResponseWrapper
import com.njkim.reactivecrypto.bithumb.model.BithumbTickData
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime

class BithumbWebsocketClient : ExchangeWebsocketClient {
    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://wss.bithumb.com/public"

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val subscribeRequests = subscribeTargets.stream()
            .map {
                if (it.baseCurrency == Currency.KRW) {
                    "${it.targetCurrency}"
                } else {
                    "${it.targetCurrency}${it.baseCurrency}"
                }
            }
            .map { "{\"currency\":\"$it\",\"tickDuration\":\"24H\",\"service\":\"transaction\"}" }
            .toFlux()

        return HttpClient.create()
            .headers { it.add("Origin", "https://www.bithumb.com") }
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { BithumbJsonObjectMapper.instance.readValue<BithumbResponseWrapper<List<BithumbTickData>>>(it) }
            .flatMapIterable {
                it.data.map { bithumbTickData ->
                    TickData(
                        bithumbTickData.countNo.toString(),
                        bithumbTickData.transactionDate,
                        bithumbTickData.price,
                        bithumbTickData.unitsTraded,
                        CurrencyPair(it.header.currency, Currency.KRW), // Bithumb only have KRW market
                        ExchangeVendor.BITHUMB
                    )
                }
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeRequests = subscribeTargets.stream()
            .map {
                if (it.baseCurrency == Currency.KRW) {
                    "${it.targetCurrency}"
                } else {
                    "${it.targetCurrency}${it.baseCurrency}"
                }
            }
            .map { "{\"currency\":\"$it\",\"tickDuration\":\"24H\",\"service\":\"orderbook\"}" }
            .toFlux()

        return HttpClient.create()
            .headers { it.add("Origin", "https://www.bithumb.com") }
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { BithumbJsonObjectMapper.instance.readValue<BithumbResponseWrapper<BithumbOrderBook>>(it) }
            .map {
                OrderBook(
                    "${it.header.currency}${ZonedDateTime.now().toInstant().toEpochMilli()}",
                    CurrencyPair(it.header.currency, Currency.KRW), // Bithumb only have KRW market
                    ZonedDateTime.now(),
                    ExchangeVendor.BITHUMB,
                    it.data.bids.map { bithumbBid ->
                        OrderBookUnit(
                            bithumbBid.price,
                            bithumbBid.quantity,
                            OrderSideType.BID,
                            null
                        )
                    },
                    it.data.asks.map { bithumbAsk ->
                        OrderBookUnit(
                            bithumbAsk.price,
                            bithumbAsk.quantity,
                            OrderSideType.ASK,
                            null
                        )
                    }
                )
            }

    }
}