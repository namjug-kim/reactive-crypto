package com.njkim.reactivecrypto.poloniex

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.poloniex.model.PoloniexEventType
import com.njkim.reactivecrypto.poloniex.model.PoloniexMessageFrame
import com.njkim.reactivecrypto.poloniex.model.PoloniexOrderBookSnapshotEvent
import reactor.core.publisher.Flux
import reactor.kotlin.core.publisher.toFlux
import reactor.netty.http.client.HttpClient
import reactor.netty.http.client.WebsocketClientSpec

class PoloniexRawWebsocketClient {
    private val baseUrl: String = "wss://api2.poloniex.com"

    /**
     * Subscribe to price aggregated depth of book by currency pair.
     * Response includes an initial book snapshot, book modifications, and trades.
     * Book modification updates with 0 quantity should be treated as removal of the price level.
     * Note that the updates are price aggregated and do not contain individual orders.
     *
     */
    fun priceAggregatedBook(currencyPairs: List<CurrencyPair>): Flux<PoloniexMessageFrame> {
        val channelIdCurrencyPairMap: MutableMap<Long, CurrencyPair> = HashMap()

        // { "command": "subscribe", "channel": "<channel id>" }
        val subscribeChannels = currencyPairs
            .map { "${it.quoteCurrency}_${it.baseCurrency}" }
            .map { "{ \"command\": \"subscribe\", \"channel\": \"$it\" }" }
            .toFlux()

        // TODO heartbeat check
        return HttpClient.create()
            .websocket(WebsocketClientSpec.builder().maxFramePayloadLength(655360).build())
            .uri(baseUrl)
            .handle { inbound, outbound ->
                outbound.sendString(subscribeChannels)
                    .then()
                    .thenMany(inbound.receive().asString())
            }
            .filter { !it.contains("{\"error\":\"") }
            .filter { it != "[1010]" } // ping message
            .map { PoloniexJsonObjectMapper.instance.readValue<PoloniexMessageFrame>(it) }
            // set currencyPair info for each channel
            .doOnNext { messageFrame ->
                val orderBookSnapshotEvent = messageFrame.events
                    .filter { it.eventType == PoloniexEventType.ORDER_BOOK_SNAPSHOT }
                    .map { it as PoloniexOrderBookSnapshotEvent }
                    .firstOrNull()

                if (orderBookSnapshotEvent != null) {
                    val channelId = messageFrame.channelId
                    channelIdCurrencyPairMap[channelId] = orderBookSnapshotEvent.currencyPair
                }
            }
            .doOnNext { messageFrame ->
                val channelId = messageFrame.channelId
                messageFrame.events
                    .forEach { event ->
                        event.currencyPair = channelIdCurrencyPairMap[channelId]!!
                    }
            }
            .doFinally {
                channelIdCurrencyPairMap.clear()
            }
    }
}
