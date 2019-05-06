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

package com.njkim.reactivecrypto.kraken

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.kraken.model.KrakenOrderBook
import com.njkim.reactivecrypto.kraken.model.KrakenOrderBookUnit
import com.njkim.reactivecrypto.kraken.model.KrakenTickData
import com.njkim.reactivecrypto.kraken.model.KrakenTickDataWrapper
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class KrakenJsonObjectMapper : ExchangeJsonObjectMapper {

    companion object {
        val instance: ObjectMapper = KrakenJsonObjectMapper().objectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochMilli(p.valueAsLong).atZone(ZoneId.systemDefault())
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair>? {
        return object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val splits = p.valueAsString.split("/")
                return CurrencyPair.parse(splits[0], splits[1])
            }
        }
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
                return BigDecimal.valueOf(p.valueAsDouble)
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return object : JsonDeserializer<TradeSideType>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                val valueAsString = p.valueAsString
                return when (valueAsString) {
                    "b" -> TradeSideType.BUY
                    "s" -> TradeSideType.SELL
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val tickDataDeserializer = object : JsonDeserializer<KrakenTickDataWrapper>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KrakenTickDataWrapper {
                val jsonNode: JsonNode = p.codec.readTree(p)

                val channelId: Int = jsonNode.get(0).asInt()
                val krakenTickData: List<KrakenTickData> = jsonNode.get(1).toList()
                    .map {
                        KrakenTickData(
                            Instant.ofEpochMilli((it.get(2).asDouble() * 1000).toLong())
                                .plusNanos((it.get(2).asDouble() * 1000000 % 1000).toLong())
                                .atZone(ZoneId.systemDefault()),
                            instance.convertValue(it.get(0).asText()),
                            instance.convertValue(it.get(1).asText()),
                            instance.convertValue(it.get(3).asText()),
                            it.get(4).asText()
                        )
                    }

                return KrakenTickDataWrapper(channelId, krakenTickData)
            }
        }

        val orderBookDeserializer = object : JsonDeserializer<KrakenOrderBook>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KrakenOrderBook {
                val jsonNode: JsonNode = p.codec.readTree(p)

                val channelId: Int = jsonNode.get(0).asInt()

                val orderBookNode = jsonNode.get(1)
                val krakenAsks: JsonNode? = when {
                    orderBookNode.has("as") -> orderBookNode.get("as")
                    else -> orderBookNode.get("a")
                }
                val asks = (krakenAsks?.toList() ?: listOf())
                    .map {
                        KrakenOrderBookUnit(
                            instance.convertValue(it.get(0).asText()),
                            instance.convertValue(it.get(1).asText()),
                            Instant.ofEpochMilli((it.get(2).asDouble() * 1000).toLong())
                                .plusNanos((it.get(2).asDouble() * 1000000 % 1000).toLong())
                                .atZone(ZoneId.systemDefault())
                        )
                    }
                val krakenBids = if (orderBookNode.has("bs")) orderBookNode.get("bs") else orderBookNode.get("b")
                val bids = (krakenBids?.toList() ?: listOf())
                    .toList()
                    .map {
                        KrakenOrderBookUnit(
                            instance.convertValue(it.get(0).asText()),
                            instance.convertValue(it.get(1).asText()),
                            Instant.ofEpochMilli((it.get(2).asDouble() * 1000).toLong())
                                .plusNanos((it.get(2).asDouble() * 1000000 % 1000).toLong())
                                .atZone(ZoneId.systemDefault())
                        )
                    }

                val updateOnly = jsonNode.has("as") && jsonNode.has("bs")
                return KrakenOrderBook(channelId, asks, bids, updateOnly)
            }
        }

        simpleModule.addDeserializer(KrakenTickDataWrapper::class.java, tickDataDeserializer)
        simpleModule.addDeserializer(KrakenOrderBook::class.java, orderBookDeserializer)
    }
}