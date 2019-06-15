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

package com.njkim.reactivecrypto.poloniex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.poloniex.model.*
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class PoloniexJsonObjectMapper : ExchangeJsonObjectMapper {

    companion object {
        val instance: ObjectMapper = com.njkim.reactivecrypto.poloniex.PoloniexJsonObjectMapper().objectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime> {
        return object : JsonDeserializer<ZonedDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochSecond(p.valueAsLong).atZone(ZoneId.systemDefault())
            }
        }
    }

    /**
     * {baseCurrency}_{targetCurrency}
     */
    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair> {
        return object : JsonDeserializer<CurrencyPair>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val currencyPairRawValue = p.valueAsString
                val split = currencyPairRawValue.split("_")

                return CurrencyPair.parse(split[1], split[0])
            }
        }
    }

    override fun orderSideTypeDeserializer(): JsonDeserializer<OrderSideType>? {
        // <1 for bid 0 for ask>
        return object : JsonDeserializer<OrderSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OrderSideType {
                return when (p.valueAsInt) {
                    0 -> OrderSideType.ASK
                    1 -> OrderSideType.BID
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        // <1 for buy 0 for sell>
        return object : JsonDeserializer<TradeSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return when (p.valueAsInt) {
                    0 -> TradeSideType.SELL
                    1 -> TradeSideType.BUY
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val poloniexMessageFrameDeserializer = object : JsonDeserializer<PoloniexMessageFrame>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PoloniexMessageFrame {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val channelId = jsonNode[0].asLong()
                val sequenceNumber = jsonNode[1].asLong()
                val events = jsonNode[2].map { event ->
                    val eventType = PoloniexEventType.parse(event[0].asText())
                    PoloniexJsonObjectMapper.instance.convertValue(event, eventType.classType)
                }

                return PoloniexMessageFrame(
                    channelId, sequenceNumber, events
                )
            }
        }

        // ["t", "<trade id>", <1 for buy 0 for sell>, "<price>", "<size>", <timestamp>] ]
        val tradeEventDeserializer = object : JsonDeserializer<PoloniexTradeEvent>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PoloniexTradeEvent {
                val jsonNode: JsonNode = p.codec.readTree(p)

                val tradeId: String = jsonNode[1].asText()
                val side: TradeSideType = instance.convertValue(jsonNode[2], TradeSideType::class.java)
                val price: BigDecimal = instance.convertValue(jsonNode[3], BigDecimal::class.java)
                val size: BigDecimal = instance.convertValue(jsonNode[4], BigDecimal::class.java)
                val timestamp: ZonedDateTime = instance.convertValue(jsonNode[5], ZonedDateTime::class.java)

                return PoloniexTradeEvent(
                    tradeId,
                    side,
                    price,
                    size,
                    timestamp
                )
            }
        }

        val orderBookUpdateEventDeserializer = object : JsonDeserializer<PoloniexOrderBookUpdateEvent>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PoloniexOrderBookUpdateEvent {
                val jsonNode: JsonNode = p.codec.readTree(p)

                val side: OrderSideType = instance.convertValue(jsonNode[1], OrderSideType::class.java)
                val price: BigDecimal = instance.convertValue(jsonNode[2], BigDecimal::class.java)
                val size: BigDecimal = instance.convertValue(jsonNode[3], BigDecimal::class.java)

                return PoloniexOrderBookUpdateEvent(
                    side,
                    price,
                    size
                )
            }
        }

        /**
         * [
         * "i",
         * {
         * "currencyPair": "<currency pair name>",
         * "orderBook": [
         * { "<lowest ask price>": "<lowest ask size>", "<next ask price>": "<next ask size>", ... },
         * { "<highest bid price>": "<highest bid size>", "<next bid price>": "<next bid size>", ... }
         * ]
         * }
         * ]
         */
        val orderBookSnapshotEventDeserializer = object : JsonDeserializer<PoloniexOrderBookSnapshotEvent>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): PoloniexOrderBookSnapshotEvent {
                val jsonNode: JsonNode = p.codec.readTree(p)

                val orderBookSnapshotNode = jsonNode.get(1)
                val currencyPair =
                    instance.convertValue(orderBookSnapshotNode.get("currencyPair"), CurrencyPair::class.java)
                val orderBookNode = orderBookSnapshotNode.get("orderBook")
                val asksNode = orderBookNode.get(0)
                val asks = asksNode.fields().asSequence().toList().map { mutableEntry ->
                    val price = mutableEntry.key
                    val size = mutableEntry.value

                    OrderBookUnit(
                        instance.convertValue(price, BigDecimal::class.java),
                        instance.convertValue(size, BigDecimal::class.java),
                        OrderSideType.ASK
                    )
                }

                val bidsNode = orderBookNode.get(1)
                val bids = bidsNode.fields().asSequence().toList().map { mutableEntry ->
                    val price = mutableEntry.key
                    val size = mutableEntry.value

                    OrderBookUnit(
                        instance.convertValue(price, BigDecimal::class.java),
                        instance.convertValue(size, BigDecimal::class.java),
                        OrderSideType.BID
                    )
                }

                return PoloniexOrderBookSnapshotEvent(
                    currencyPair,
                    bids,
                    asks
                )
            }
        }

        simpleModule.addDeserializer(PoloniexMessageFrame::class.java, poloniexMessageFrameDeserializer)
        simpleModule.addDeserializer(PoloniexTradeEvent::class.java, tradeEventDeserializer)
        simpleModule.addDeserializer(PoloniexOrderBookUpdateEvent::class.java, orderBookUpdateEventDeserializer)
        simpleModule.addDeserializer(PoloniexOrderBookSnapshotEvent::class.java, orderBookSnapshotEventDeserializer)
    }
}
