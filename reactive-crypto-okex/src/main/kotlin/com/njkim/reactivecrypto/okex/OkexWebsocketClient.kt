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

package com.njkim.reactivecrypto.okex

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.websocket.AbstractExchangeWebsocketClient
import com.njkim.reactivecrypto.okex.model.OkexOrderBookWrapper
import com.njkim.reactivecrypto.okex.model.OkexTickDataWrapper
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import mu.KotlinLogging
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.math.BigDecimal
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.concurrent.ConcurrentHashMap
import kotlin.streams.toList

open class OkexWebsocketClient(
    private val baseUri: String = "wss://real.okex.com:10442/ws/v3"
) : AbstractExchangeWebsocketClient() {
    private val log = KotlinLogging.logger {}

    private val objectMapper: ObjectMapper = createJsonObjectMapper().objectMapper()

    override fun createJsonObjectMapper(): ExchangeJsonObjectMapper {
        return OkexJsonObjectMapper()
    }

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeMessages = subscribeTargets.stream()
            .map { "${it.targetCurrency.symbol}-${it.baseCurrency.symbol}" }
            .map { "{\"op\": \"subscribe\", \"args\": [\"spot/depth:$it\"]}" }
            .toList()

        val currentOrderBookMap: MutableMap<CurrencyPair, OrderBook> = ConcurrentHashMap()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp -> tcp.doOnConnected { connection -> connection.addHandler(Deflat64Decoder()) } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.fromIterable<String>(subscribeMessages))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .doOnNext { log.debug { it } }
            .filter { it.contains("\"spot/depth\"") }
            .map { objectMapper.readValue<OkexOrderBookWrapper>(it) }
            .map { it.data }
            .flatMapIterable {
                it.map { okexTickData ->
                    val now = ZonedDateTime.now()
                    OrderBook(
                        "${okexTickData.instrumentId}${now.toEpochMilli()}",
                        okexTickData.instrumentId,
                        now,
                        ExchangeVendor.OKEX,
                        okexTickData.getBids().toMutableList(),
                        okexTickData.getAsks().toMutableList()
                    )
                }
            }
            .map { orderBook ->
                if (!currentOrderBookMap.containsKey(orderBook.currencyPair)) {
                    currentOrderBookMap[orderBook.currencyPair] = orderBook
                    return@map orderBook
                }

                val prevOrderBook = currentOrderBookMap[orderBook.currencyPair]!!

                val askMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.asks
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.asks.forEach { updatedAsk ->
                    askMap.compute(updatedAsk.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedAsk.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedAsk
                            else -> oldValue.copy(
                                quantity = updatedAsk.quantity,
                                orderNumbers = updatedAsk.orderNumbers
                            )
                        }
                    }
                }

                val bidMap: MutableMap<BigDecimal, OrderBookUnit> = prevOrderBook.bids
                    .map { Pair(it.price.stripTrailingZeros(), it) }
                    .toMap()
                    .toMutableMap()

                orderBook.bids.forEach { updatedBid ->
                    bidMap.compute(updatedBid.price.stripTrailingZeros()) { _, oldValue ->
                        when {
                            updatedBid.quantity <= BigDecimal.ZERO -> null
                            oldValue == null -> updatedBid
                            else -> oldValue.copy(
                                quantity = updatedBid.quantity,
                                orderNumbers = updatedBid.orderNumbers
                            )
                        }
                    }
                }

                val currentOrderBook = prevOrderBook.copy(
                    eventTime = orderBook.eventTime,
                    asks = askMap.values.sortedBy { orderBookUnit -> orderBookUnit.price },
                    bids = bidMap.values.sortedByDescending { orderBookUnit -> orderBookUnit.price }
                )
                currentOrderBookMap[currentOrderBook.currencyPair] = currentOrderBook
                currentOrderBook
            }
            .doFinally { currentOrderBookMap.clear() } // cleanup memory limit orderBook when disconnected
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val subscribeMessages = subscribeTargets.stream()
            .map { "${it.targetCurrency.symbol}-${it.baseCurrency.symbol}" }
            .map { "{\"op\": \"subscribe\", \"args\": [\"spot/trade:$it\"]}" }
            .toList()

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(Deflat64Decoder())
                }
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.fromIterable<String>(subscribeMessages))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { t -> t.contains("\"spot/trade\"") }
            .map { objectMapper.readValue<OkexTickDataWrapper>(it) }
            .map { it.data }
            .flatMapIterable {
                it.map { okexTickData ->
                    TickData(
                        okexTickData.tradeId,
                        okexTickData.timestamp,
                        okexTickData.price,
                        okexTickData.size,
                        okexTickData.instrumentId,
                        ExchangeVendor.OKEX,
                        okexTickData.side
                    )
                }
            }
            .doOnError { log.error(it.message, it) }
    }

    private inner class Deflat64Decoder : ByteToMessageDecoder() {
        override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
            Deflate64CompressorInputStream(ByteBufInputStream(msg)).use {
                val responseBody = StreamUtils.copyToString(it, Charset.forName("UTF-8"))
                val uncompressed = msg.alloc().buffer().writeBytes(responseBody.toByteArray())
                out.add(uncompressed)
            }
        }
    }
}
