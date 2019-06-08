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

package com.njkim.reactivecrypto.coineal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.coineal.model.CoinealMessageFrame
import com.njkim.reactivecrypto.coineal.model.CoinealOrderBook
import com.njkim.reactivecrypto.coineal.model.CoinealTickDataWrapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.compression.JdkZlibDecoder
import io.netty.handler.codec.compression.ZlibWrapper
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import mu.KotlinLogging
import org.apache.commons.codec.Charsets
import org.apache.commons.lang3.StringUtils
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

@Suppress("IMPLICIT_CAST_TO_ANY")
class CoinealRawWebsocketClient(
    private val baseUri: String = "wss://ws.coineal.com/kline-api/ws"
) {
    private val log = KotlinLogging.logger {}

    companion object {
        val objectMapper: ObjectMapper = CoinealJsonObjectMapper().objectMapper()
    }

    fun createTradeDataFlux(symbols: List<CurrencyPair>): Flux<CoinealMessageFrame<CoinealTickDataWrapper>> {
        val subscribeStrings = symbols.map { "${it.targetCurrency}${it.baseCurrency}".toLowerCase() }
            .map { "{\"event\":\"sub\",\"params\":{\"channel\":\"market_${it}_trade_ticker\",\"cb_id\":\"$it\"}}" }
            .toFlux()

        return createSubscribeRequest(subscribeStrings)
            .map { objectMapper.readValue<CoinealMessageFrame<CoinealTickDataWrapper>>(it) }
    }

    private fun createSubscribeRequest(subscribeStrings: Flux<String>): Flux<String> {
        return HttpClient.create()
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(JdkZlibDecoder(ZlibWrapper.GZIP, true))
                    connection.addHandler(PingPongHandler())
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            10,
                            TimeUnit.SECONDS,
                            5
                        ) { "{\"ping\": ${ZonedDateTime.now().toEpochMilli()}}" }
                    )
                }
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeStrings)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { !it.contains("\"event_rep\":\"subed\"") }
    }

    /**
     * @param type Depth type, step0, step1, step2 (combined depth 0-2); step0, the highest precision,
     * generally 1 times, 10 times, 100 times the precision
     */
    fun createOrderBookFlux(symbols: List<CurrencyPair>, type: String): Flux<CoinealMessageFrame<CoinealOrderBook>> {
        val subscribeStrings = symbols.map { "${it.targetCurrency}${it.baseCurrency}".toLowerCase() }
            .map { "{\"event\":\"sub\",\"params\":{\"channel\":\"market_${it}_depth_$type\",\"cb_id\":\"${it}\"}}" }
            .toFlux()

        return createSubscribeRequest(subscribeStrings)
            .map { objectMapper.readValue<CoinealMessageFrame<CoinealOrderBook>>(it) }
    }

    /**
     * server sent ping {"ping" : $epochMilli }
     * client response pong {"pong" : $epochMilli }
     */
    private inner class PingPongHandler : ByteToMessageDecoder() {
        override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
            ByteBufInputStream(msg).use {
                val response = StreamUtils.copyToString(it, Charsets.UTF_8)
                if (StringUtils.contains(response, "ping")) {
                    val replace = response.replace("ping", "pong")
                    ctx.channel().writeAndFlush(TextWebSocketFrame(replace))
                } else {
                    val uncompressed = msg.alloc().buffer().writeBytes(response.toByteArray())
                    out.add(uncompressed)
                }
            }
        }
    }
}