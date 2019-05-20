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

package com.njkim.reactivecrypto.idax

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
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
import com.njkim.reactivecrypto.idax.model.IdaxOrderBook
import com.njkim.reactivecrypto.idax.model.IdaxTickData
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class IdaxJsonObjectMapper : ExchangeJsonObjectMapper {

    companion object {
        val instance: ObjectMapper = IdaxJsonObjectMapper().objectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochMilli(p.valueAsLong).atZone(ZoneId.systemDefault())
            }
        }
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
                return BigDecimal(p.valueAsString)
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair> {
        return object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val split = p.valueAsString.split("_")
                return CurrencyPair.parse(split[0], split[1])
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType> {
        return object : JsonDeserializer<TradeSideType>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return TradeSideType.valueOf(p.valueAsString.toUpperCase())
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val tickDataDeserializer = object : JsonDeserializer<IdaxTickData>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IdaxTickData {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val transactionNumber = jsonNode.get(0).asText()
                val priceString = jsonNode.get(1).asText()
                val volumeString = jsonNode.get(2).asText()
                val eventDateTimeStamp = jsonNode.get(3).asLong()
                val tradeSideTypeString = jsonNode.get(4).asText()

                return IdaxTickData(
                    transactionNumber,
                    instance.convertValue(priceString, BigDecimal::class.java),
                    instance.convertValue(volumeString, BigDecimal::class.java),
                    instance.convertValue(eventDateTimeStamp, ZonedDateTime::class.java),
                    instance.convertValue(tradeSideTypeString, TradeSideType::class.java)
                )
            }
        }

        val orderBookDeserializer = object : JsonDeserializer<IdaxOrderBook>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): IdaxOrderBook {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val bidNode = jsonNode.get("bids")
                val askNode = jsonNode.get("asks")

                val timestampLong = jsonNode.get("timestamp").asLong()

                val bids = bidNode.map {
                    OrderBookUnit(
                        instance.convertValue(it.get(0).asText(), BigDecimal::class.java),
                        instance.convertValue(it.get(1).asText(), BigDecimal::class.java),
                        OrderSideType.BID
                    )
                }

                val asks = askNode.map {
                    OrderBookUnit(
                        instance.convertValue(it.get(0).asText(), BigDecimal::class.java),
                        instance.convertValue(it.get(1).asText(), BigDecimal::class.java),
                        OrderSideType.ASK
                    )
                }

                return IdaxOrderBook(
                    bids,
                    asks,
                    instance.convertValue(timestampLong, ZonedDateTime::class.java)
                )
            }
        }

        simpleModule.addDeserializer(IdaxTickData::class.java, tickDataDeserializer)
        simpleModule.addDeserializer(IdaxOrderBook::class.java, orderBookDeserializer)
    }
}