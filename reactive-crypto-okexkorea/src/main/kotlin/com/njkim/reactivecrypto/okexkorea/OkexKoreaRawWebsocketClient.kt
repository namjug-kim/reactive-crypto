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

package com.njkim.reactivecrypto.okexkorea

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import com.njkim.reactivecrypto.okexkorea.model.OkexKoreaDepthMessage
import com.njkim.reactivecrypto.okexkorea.model.OkexKoreaMessageFrame
import com.njkim.reactivecrypto.okexkorea.model.OkexKoreaTickDataMessageFrame
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import java.nio.charset.Charset
import java.util.concurrent.TimeUnit

class OkexKoreaRawWebsocketClient {
    private val baseUrl = "wss://okexcomreal.bafang.com:10441/websocket?brokerId=ODk"

    fun createDepthFlux(symbols: List<CurrencyPair>): Flux<OkexKoreaMessageFrame<OkexKoreaDepthMessage>> {
        // ex) {event:"addChannel", parameters:{base: "etc", binary: "1", product: "spot", quote: "btc", type:"depth"}}
        val subscribeRequests = symbols
            .map {
                "{event:\"addChannel\", parameters:{" +
                        "base: \"${it.targetCurrency.name.toLowerCase()}\"," +
                        " binary: \"1\"," +
                        " product: \"spot\"," +
                        " quote: \"${it.baseCurrency.name.toLowerCase()}\"," +
                        " type:\"depth\"}}"
            }
            .toFlux()

        return HttpClient.create()
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(Deflat64Decoder())
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            5,
                            TimeUnit.SECONDS,
                            5
                        ) { "{\"event\": \"ping\"}" }
                    )
                }
            }
            .websocket()
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { !it.contains("\"channel\":\"addChannel\"") }
            .filter { !it.contains("{\"event\":\"pong\"}") }
            .flatMapIterable {
                OkexKoreaJsonObjectMapper.instance.readValue<List<OkexKoreaMessageFrame<OkexKoreaDepthMessage>>>(it)
            }
    }

    fun createTickDataFlux(symbols: List<CurrencyPair>): Flux<OkexKoreaTickDataMessageFrame> {
        val subscribeRequests = symbols
            .map { "${it.targetCurrency}_${it.baseCurrency}".toLowerCase() }
            .map {
                "{event:\"addChannel\", channel:\"ok_sub_spot_${it}_deals\"}\t"
            }
            .toFlux()

        return HttpClient.create()
            .tcpConfiguration { tcp ->
                tcp.doOnConnected { connection ->
                    connection.addHandler(Deflat64Decoder())
                    connection.addHandler(
                        "heartBeat",
                        HeartBeatHandler(
                            false,
                            5,
                            TimeUnit.SECONDS,
                            5
                        ) { "{\"event\": \"ping\"}" }
                    )
                }
            }
            .websocket()
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeRequests)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { !it.contains("\"channel\":\"addChannel\"") }
            .filter { !it.contains("{\"event\":\"pong\"}") }
            .flatMapIterable { OkexKoreaJsonObjectMapper.instance.readValue<List<OkexKoreaTickDataMessageFrame>>(it) }
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
