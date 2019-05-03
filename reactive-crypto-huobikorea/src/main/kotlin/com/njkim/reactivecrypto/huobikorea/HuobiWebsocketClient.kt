package com.njkim.reactivecrypto.huobikorea

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.ExchangeWebsocketClient
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBook
import com.njkim.reactivecrypto.core.common.model.order.TickData
import com.njkim.reactivecrypto.huobikorea.model.HuobiKoreaTickDataWrapper
import com.njkim.reactivecrypto.huobikorea.model.HuobiOrderBook
import com.njkim.reactivecrypto.huobikorea.model.HuobiSubscribeResponse
import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufInputStream
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import org.springframework.util.StreamUtils
import reactor.core.publisher.Flux
import reactor.netty.http.client.HttpClient
import java.nio.charset.Charset
import java.time.ZonedDateTime
import java.util.zip.GZIPInputStream
import kotlin.streams.toList

class HuobiWebsocketClient : ExchangeWebsocketClient {

    private val log = KotlinLogging.logger {}

    private val baseUri = "wss://api-cloud.huobi.co.kr/ws"

    override fun createDepthSnapshot(subscribeTargets: List<CurrencyPair>): Flux<OrderBook> {
        val subscribeMessages = subscribeTargets.stream()
            .map { "${it.targetCurrency.name.toLowerCase()}${it.baseCurrency.name.toLowerCase()}" }
            .map { "{\"sub\": \"market.$it.depth.step0\",\"id\": \"$it\"}" }
            .toList()


        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp -> tcp.doOnConnected { connection -> connection.addHandler(GzipDecoder()) } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.fromIterable<String>(subscribeMessages))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .doOnNext { log.debug { it } }
            .filter { it.contains("\"ch\"") }
            .map { HuobiJsonObjectMapper.instance.readValue<HuobiSubscribeResponse<HuobiOrderBook>>(it) }
            .map {
                OrderBook(
                    "${it.currencyPair}${it.ts.toInstant().toEpochMilli()}",
                    it.currencyPair,
                    ZonedDateTime.now(),
                    ExchangeVendor.HUOBI_KOREA,
                    it.tick.getBids(),
                    it.tick.getAsks()
                )
            }
    }

    override fun createTradeWebsocket(subscribeTargets: List<CurrencyPair>): Flux<TickData> {
        val subscribeMessages = subscribeTargets.stream()
            .map { "${it.targetCurrency.name.toLowerCase()}${it.baseCurrency.name.toLowerCase()}" }
            .map { "{\"sub\": \"market.$it.trade.detail\",\"id\": \"$it\"}" }
            .toList()


        return HttpClient.create()
            .wiretap(log.isDebugEnabled)
            .tcpConfiguration { tcp -> tcp.doOnConnected { connection -> connection.addHandler(GzipDecoder()) } }
            .websocket()
            .uri(baseUri)
            .handle { inbound, outbound ->
                outbound.sendString(Flux.fromIterable<String>(subscribeMessages))
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { it.contains("\"ch\"") }
            .map { HuobiJsonObjectMapper.instance.readValue<HuobiSubscribeResponse<HuobiKoreaTickDataWrapper>>(it) }
            .flatMapIterable {
                it.tick.data
                    .map { huobiKoreaTickData ->
                        TickData(
                            huobiKoreaTickData.id.toPlainString(),
                            huobiKoreaTickData.ts,
                            huobiKoreaTickData.price,
                            huobiKoreaTickData.amount,
                            it.currencyPair,
                            ExchangeVendor.HUOBI_KOREA
                        )
                    }
                    .toList()
            }
    }

    private inner class GzipDecoder : ByteToMessageDecoder() {
        @Throws(Exception::class)
        override fun decode(ctx: ChannelHandlerContext, msg: ByteBuf, out: MutableList<Any>) {
            val gzipInputStream = GZIPInputStream(ByteBufInputStream(msg))
            val responseBody = StreamUtils.copyToString(gzipInputStream, Charset.forName("UTF-8"))

            if (StringUtils.contains(responseBody, "ping")) {
                val replace = responseBody.replace("ping", "pong")
                ctx.channel().writeAndFlush(TextWebSocketFrame(replace))
            } else {
                val uncompressed = msg.alloc().buffer().writeBytes(responseBody.toByteArray())
                out.add(uncompressed)
            }
        }
    }
}