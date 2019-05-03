package com.njkim.reactivecrypto.upbit

import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.upbit.model.UpbitOrderBook
import com.njkim.reactivecrypto.upbit.model.UpbitTickData
import io.netty.handler.codec.json.JsonObjectDecoder
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import java.util.stream.Collectors
import kotlin.streams.toList

class UpbitWebsocketClient : ExchangeWebsocketClient {
    private val baseUri: String = "wss://api.upbit.com/websocket/v1"

    private val lastOrderBookTimestamp = AtomicLong()
    private val orderBookTimestampDuplicateCount = AtomicInteger()

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        // CoinSymbol: {rightCurrency}-{leftCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.baseCurrency}-${currencyPair.targetCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .tcpConfiguration { t -> t.doOnConnected { it.addHandlerLast(JsonObjectDecoder()) } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"trade\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { UpbitJsonObjectMapper.instance.readValue(it, UpbitTickData::class.java) }
            .map {
                TickData(
                    it.sequentialId.toString() + it.code,
                    it.tradeTimestamp,
                    it.tradePrice,
                    it.tradeVolume,
                    it.code,
                    ExchangeVendor.UPBIT
                )
            }
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        // CoinSymbol: {targetCurrency}-{baseCurrency}
        val coinSymbols = subscribeTargets.stream()
            .map<String> { currencyPair -> "\"${currencyPair.baseCurrency}-${currencyPair.targetCurrency}\"" }
            .collect(Collectors.joining(","))

        return HttpClient.create()
            .tcpConfiguration { tcp -> tcp.doOnConnected { } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just("[{\"ticket\":\"UNIQUE_TICKET\"},{\"type\":\"orderbook\",\"codes\":[$coinSymbols]}]"))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .map { UpbitJsonObjectMapper.instance.readValue(it, UpbitOrderBook::class.java) }
            .map {
                OrderBook(
                    createOrderBookUniqueId(it.timestamp.toInstant().toEpochMilli()),
                    it.code,
                    ZonedDateTime.now(),
                    ExchangeVendor.UPBIT,
                    it.orderBookUnits.stream()
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.bidPrice,
                                orderBookUnit.bidSize,
                                OrderSideType.ASK
                            )
                        }
                        .toList(),
                    it.orderBookUnits.stream()
                        .map { orderBookUnit ->
                            OrderBookUnit(
                                orderBookUnit.askPrice,
                                orderBookUnit.askSize,
                                OrderSideType.ASK
                            )
                        }
                        .toList()
                )
            }
    }

    /**
     * add salt value for create unique Id
     */
    private fun createOrderBookUniqueId(upbitTimestamp: Long): String {
        return if (lastOrderBookTimestamp.getAndSet(upbitTimestamp) == upbitTimestamp) {
            "${upbitTimestamp + orderBookTimestampDuplicateCount.incrementAndGet()}"
        } else {
            orderBookTimestampDuplicateCount.set(0)
            "$upbitTimestamp"
        }
    }
}