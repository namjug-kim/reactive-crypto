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

package com.njkim.reactivecrypto.bitstamp

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.bitstamp.model.BitstampDetailOrderBook
import com.njkim.reactivecrypto.bitstamp.model.BitstampDetailOrderBookUnit
import com.njkim.reactivecrypto.bitstamp.model.BitstampOrderBook
import com.njkim.reactivecrypto.bitstamp.model.BitstampOrderBookUnit
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

class BitstampJsonObjectMapper : ExchangeJsonObjectMapper {

    companion object {
        val instance: ObjectMapper = BitstampJsonObjectMapper().objectMapper()
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal> {
        return object : JsonDeserializer<BigDecimal>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
                return BigDecimal(p.valueAsString)
            }
        }
    }

    // "1560675931555480"
    // "1560675931"
    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime> {
        return object : JsonDeserializer<ZonedDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return if (p.valueAsLong > 1000000000000000) {
                    Instant.ofEpochSecond(TimeUnit.MICROSECONDS.toSeconds(p.valueAsLong), p.valueAsLong % 1000000)
                        .atZone(ZoneId.systemDefault())
                } else {
                    Instant.ofEpochSecond(p.valueAsLong)
                        .atZone(ZoneId.systemDefault())
                }
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        // Trade type (0 - buy; 1 - sell).
        return object : JsonDeserializer<TradeSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return when (p.valueAsInt) {
                    0 -> TradeSideType.BUY
                    1 -> TradeSideType.SELL
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val orderBookUnitDeserializer = object : JsonDeserializer<BitstampOrderBookUnit>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitstampOrderBookUnit {
                val jsonNode: JsonNode = p.codec.readTree(p)
                return BitstampOrderBookUnit(
                    instance.convertValue(jsonNode[0], BigDecimal::class.java),
                    instance.convertValue(jsonNode[1], BigDecimal::class.java)
                )
            }
        }

        val orderBookDeserializer = object : JsonDeserializer<BitstampOrderBook>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitstampOrderBook {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val timestampNode = jsonNode.get("timestamp")
                val microtimestampNode = jsonNode.get("microtimestamp")
                val bidsNode = jsonNode.get("bids")
                val asksNode = jsonNode.get("asks")

                return BitstampOrderBook(
                    instance.convertValue(microtimestampNode, ZonedDateTime::class.java),
                    instance.convertValue(timestampNode, ZonedDateTime::class.java),
                    bidsNode.map { instance.convertValue(it, BitstampOrderBookUnit::class.java) },
                    asksNode.map { instance.convertValue(it, BitstampOrderBookUnit::class.java) }
                )
            }
        }

        val detailOrderBookUnitDeserializer = object : JsonDeserializer<BitstampDetailOrderBookUnit>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitstampDetailOrderBookUnit {
                val jsonNode: JsonNode = p.codec.readTree(p)
                return BitstampDetailOrderBookUnit(
                    instance.convertValue(jsonNode[0], BigDecimal::class.java),
                    instance.convertValue(jsonNode[1], BigDecimal::class.java),
                    jsonNode[2].asText()
                )
            }
        }

        val detailOrderBookDeserializer = object : JsonDeserializer<BitstampDetailOrderBook>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitstampDetailOrderBook {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val timestampNode = jsonNode.get("timestamp")
                val microtimestampNode = jsonNode.get("microtimestamp")
                val bidsNode = jsonNode.get("bids")
                val asksNode = jsonNode.get("asks")

                return BitstampDetailOrderBook(
                    instance.convertValue(microtimestampNode, ZonedDateTime::class.java),
                    instance.convertValue(timestampNode, ZonedDateTime::class.java),
                    bidsNode.map { instance.convertValue(it, BitstampDetailOrderBookUnit::class.java) },
                    asksNode.map { instance.convertValue(it, BitstampDetailOrderBookUnit::class.java) }
                )
            }
        }

        simpleModule.addDeserializer(BitstampOrderBookUnit::class.java, orderBookUnitDeserializer)
        simpleModule.addDeserializer(BitstampOrderBook::class.java, orderBookDeserializer)
        simpleModule.addDeserializer(BitstampDetailOrderBookUnit::class.java, detailOrderBookUnitDeserializer)
        simpleModule.addDeserializer(BitstampDetailOrderBook::class.java, detailOrderBookDeserializer)
    }
}
