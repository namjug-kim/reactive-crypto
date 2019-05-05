package com.njkim.reactivecrypto.hubi

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.hubi.model.HubiMessageFrame
import com.njkim.reactivecrypto.hubi.model.HubiOrderBook
import com.njkim.reactivecrypto.hubi.model.HubiTickDataWrapper
import mu.KotlinLogging
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong


class HubiWebsocketClient : ExchangeWebsocketClient {

    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://www.hubi.com/was"

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeSymbols = subscribeTargets.joinToString(",") {
            "${it.targetCurrency.name}${it.baseCurrency.name}".toLowerCase()
        }

        val subscribeMessage = "{" +
                "\"channel\": \"depth_all\"," +
                "\"symbol\": \"$subscribeSymbols\"," +
                "\"bourse\": \"01001\"" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.aggregateFrames(65536).receive().asString())
            }
            .filter { it.contains("\"dataType\":\"depth_all\"") }
            .map { HubiJsonObjectMapper.instance.readValue<HubiMessageFrame<HubiOrderBook>>(it) }
            .map { messageFrame ->
                val eventTime = ZonedDateTime.now()
                OrderBook(
                    "${messageFrame.symbol}${eventTime.toEpochMilli()}",
                    messageFrame.symbol,
                    eventTime,
                    ExchangeVendor.HUBI,
                    messageFrame.data.bids.map { OrderBookUnit(it.price, it.amount, OrderSideType.BID, null) },
                    messageFrame.data.asks.map { OrderBookUnit(it.price, it.amount, OrderSideType.ASK, null) }
                )
            }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val lastPublishedTimestamp: MutableMap<CurrencyPair, AtomicLong> = ConcurrentHashMap()

        val subscribeSymbols = subscribeTargets.joinToString(",") {
            "${it.targetCurrency.name}${it.baseCurrency.name}".toLowerCase()
        }

        val subscribeMessage = "{" +
                "\"channel\": \"trade_history\"," +
                "\"symbol\": \"$subscribeSymbols\"," +
                "\"bourse\": \"01001\"" +
                "}"

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .websocket(65536)
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(subscribeMessage))
                    .then()
                    .thenMany(inbound.aggregateFrames(65536).receive().asString())
            }
            .filter { it.contains("\"dataType\":\"trade_history\"") }
            .map { HubiJsonObjectMapper.instance.readValue<HubiMessageFrame<HubiTickDataWrapper>>(it) }
            .map { it.data }
            .flatMapIterable {
                it.trades
                    .takeWhile { hubiTickData ->
                        // hubi trade history response contain history data
                        val lastTradeEpochMilli =
                            lastPublishedTimestamp.computeIfAbsent(hubiTickData.symbol) { AtomicLong() }
                        val isNew = hubiTickData.time.toEpochMilli() > lastTradeEpochMilli.toLong()
                        if (isNew) {
                            lastTradeEpochMilli.set(hubiTickData.time.toEpochMilli())
                        }
                        isNew
                    }
                    .map { hubiTickData ->
                        TickData(
                            "${hubiTickData.symbol}${hubiTickData.time}",
                            hubiTickData.time,
                            hubiTickData.price,
                            hubiTickData.amount,
                            hubiTickData.symbol,
                            ExchangeVendor.HUBI
                        )
                    }
                    .reversed()
            }
            .doOnError { log.error(it.message, it) }
    }
}
