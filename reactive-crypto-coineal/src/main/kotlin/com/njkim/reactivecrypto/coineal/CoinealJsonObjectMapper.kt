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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.coineal.model.CoinealOrderBook
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class CoinealJsonObjectMapper : ExchangeJsonObjectMapper {

    companion object {
        val instance: ObjectMapper = CoinealJsonObjectMapper().objectMapper()
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
                val parse = CurrencyPairUtil.parse(p.valueAsString)
                return checkNotNull(parse)
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
        val orderBookDeserializer = object : JsonDeserializer<CoinealOrderBook>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CoinealOrderBook {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val bidNode = jsonNode.get("buys")
                val askNode = jsonNode.get("asks")

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

                return CoinealOrderBook(
                    bids,
                    asks
                )
            }
        }

        val currencyPairSerializer = object : JsonSerializer<CurrencyPair>() {
            override fun serialize(value: CurrencyPair, gen: JsonGenerator, serializers: SerializerProvider) {
                val pair = "${value.targetCurrency}${value.baseCurrency}".toLowerCase()
                return gen.writeString(pair)
            }
        }

        val bigDecimalSerializer = object : JsonSerializer<BigDecimal>() {
            override fun serialize(value: BigDecimal, gen: JsonGenerator, serializers: SerializerProvider) {
                return gen.writeString(value.toPlainString())
            }
        }

        /**
         * Order type: 1: Limit Orders, 2. Market Price Orders
         */
        val orderTypeSerializer = object : JsonSerializer<OrderType>() {
            override fun serialize(value: OrderType, gen: JsonGenerator, serializers: SerializerProvider) {
                val orderType = when (value) {
                    OrderType.LIMIT -> 1
                    OrderType.MARKET -> 2
                }
                return gen.writeNumber(orderType)
            }
        }

        simpleModule.addSerializer(CurrencyPair::class.java, currencyPairSerializer)
        simpleModule.addSerializer(BigDecimal::class.java, bigDecimalSerializer)
        simpleModule.addSerializer(OrderType::class.java, orderTypeSerializer)
        simpleModule.addDeserializer(CoinealOrderBook::class.java, orderBookDeserializer)
    }
}