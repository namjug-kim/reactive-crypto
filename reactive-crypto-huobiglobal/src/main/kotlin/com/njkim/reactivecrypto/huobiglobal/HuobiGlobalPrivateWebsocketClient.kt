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

package com.njkim.reactivecrypto.huobiglobal

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.common.model.order.EventMessage
import com.njkim.reactivecrypto.core.common.model.order.EventType
import com.njkim.reactivecrypto.core.common.model.order.OrderEvent
import com.njkim.reactivecrypto.core.common.util.CryptUtil
import com.njkim.reactivecrypto.core.common.util.toBase64String
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import com.njkim.reactivecrypto.core.netty.HeartBeatHandler
import com.njkim.reactivecrypto.core.websocket.ExchangePrivateWebsocketClient
import com.njkim.reactivecrypto.huobiglobal.model.HuobiPrivateMessageFrameV2
import com.njkim.reactivecrypto.huobiglobal.model.HuobiTradeEventV2
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import mu.KotlinLogging
import org.apache.commons.codec.Charsets
import org.apache.commons.lang3.StringUtils
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.netty.http.client.HttpClient
import java.net.URI
import java.net.URLEncoder
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("IMPLICIT_CAST_TO_ANY")
open class HuobiGlobalPrivateWebsocketClient(
    accessKey: String,
    secretKey: String
) : ExchangePrivateWebsocketClient(accessKey, secretKey) {
    private val log = KotlinLogging.logger {}
    protected open val baseUri: URI = URI("wss://api-cloud.huobi.co.kr/ws/v2")

    private val objectMapper: ObjectMapper = HuobiJsonObjectMapper.instance.objectMapper()

    override fun orderEvent(): Flux<EventMessage<OrderEvent>> {
        val subscribeRequest = """{"action": "sub","ch": "orders#*"}"""

        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .doOnConnected { connection ->
                connection.addHandler("pingpoing", PingPongHandler())
                connection.addHandler("hearthbeat",
                    HeartBeatHandler(
                        false,
                        21,
                        TimeUnit.SECONDS,
                        1
                    ) { """{"action":"ping","data":{"ts":${ZonedDateTime.now().toEpochMilli()}}}""" })
            }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.just(signatureFunction(accessKey, secretKey)))
                    .then()
                    .thenMany(inbound.receive().asString())
                    .flatMap<String> { response ->
                        when {
                            response.contains("invalid.auth.state") -> {
                                error("invalid auth state")
                            }
                            response.contains(""""ch":"auth"""") -> {
                                outbound.sendString(Mono.just(subscribeRequest))
                                    .then()
                                    .map { response }
                            }
                            else -> {
                                Mono.just(response)
                            }
                        }
                    }
            }
            .filter { it.contains(""""eventType":"trade"""") }
            .map { objectMapper.readValue<HuobiPrivateMessageFrameV2<HuobiTradeEventV2>>(it) }
            .map {
                val data = it.data
                val order = OrderEvent(
                    uniqueId = data.orderId,
                    orderStatusType = data.orderStatus.toOrderStatusType(),
                    side = data.type,
                    currencyPair = data.symbol,
                    orderPrice = data.orderPrice,
                    tradePrice = data.tradePrice,
                    averageTradePrice = null,
                    orderVolume = data.orderSize,
                    tradeVolume = data.tradeVolume,
                    filledVolume = data.orderSize - data.remainAmt,
                    createDateTime = data.tradeTime
                )
                EventMessage(
                    "${UUID.randomUUID()}",
                    ZonedDateTime.now(),
                    EventType.ORDER_FILLED,
                    order
                )
            }
    }

    private val signatureFunction: (String, String) -> String = { accessKey: String, secretKey: String ->
        val timestamp = ZonedDateTime.now()
            .withZoneSameInstant(ZoneOffset.UTC)
            .withNano(0)
            .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

        val params = mapOf(
            "accessKey" to accessKey,
            "signatureMethod" to "HmacSHA256",
            "signatureVersion" to "2.1",
            "timestamp" to timestamp
        )
            .toMap()
            .toMutableMap()

        val query = params
            .map { "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}" }
            .joinToString("&")

        val payload = "GET\n${baseUri.host}\n${baseUri.path}\n$query"
        val signature = CryptUtil.encrypt("HmacSHA256", payload.toByteArray(), secretKey.toByteArray()).toBase64String()

        params["signature"] = signature
        params["authType"] = "api"
        objectMapper.writeValueAsString(
            mapOf(
                "action" to "req",
                "ch" to "auth",
                "params" to params
            )
        )
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
