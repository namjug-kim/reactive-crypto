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

package com.njkim.reactivecrypto.kucoin

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.kucoin.model.KucoinAccount
import com.njkim.reactivecrypto.kucoin.model.KucoinOrderBookUnit
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class KucoinJsonObjectMapper : ExchangeJsonObjectMapper {
    companion object {
        val instance: ObjectMapper = KucoinJsonObjectMapper().objectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochMilli(p.valueAsLong)
                    .atZone(ZoneId.systemDefault())
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
                return CurrencyPair.parse(p.valueAsString)
            }
        }
    }

    override fun currencyDeserializer(): JsonDeserializer<Currency>? {
        return object : JsonDeserializer<Currency>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Currency {
                return Currency.getInstance(p.valueAsString.toUpperCase())
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return object : JsonDeserializer<TradeSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return TradeSideType.valueOf(p.valueAsString.toUpperCase())
            }
        }
    }

    override fun orderTypeDeserializer(): JsonDeserializer<OrderType>? {
        return object : JsonDeserializer<OrderType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OrderType {
                return OrderType.valueOf(p.valueAsString.toUpperCase())
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val orderBookUnitDeserializer = object : JsonDeserializer<KucoinOrderBookUnit>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KucoinOrderBookUnit {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val price = instance.convertValue(jsonNode[0], BigDecimal::class.java)
                val quantity = instance.convertValue(jsonNode[1], BigDecimal::class.java)
                var sequence: BigDecimal? = null
                if (jsonNode.size() > 2) {
                    sequence = instance.convertValue(jsonNode[2], BigDecimal::class.java)
                }

                return KucoinOrderBookUnit(price, quantity, sequence)
            }
        }

        val accountTypeDeserializer = object : JsonDeserializer<KucoinAccount.KucoinAccountType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): KucoinAccount.KucoinAccountType {
                return KucoinAccount.KucoinAccountType.valueOf(p.valueAsString.toUpperCase())
            }
        }

        val currencyPairSerializer = object : JsonSerializer<CurrencyPair>() {
            override fun serialize(value: CurrencyPair, gen: JsonGenerator, serializers: SerializerProvider?) {
                return gen.writeString(value.toString())
            }
        }

        val tradeSideTypeSerializer = object : JsonSerializer<TradeSideType>() {
            override fun serialize(value: TradeSideType, gen: JsonGenerator, serializers: SerializerProvider?) {
                return gen.writeString(value.name.toLowerCase())
            }
        }

        val orderTypeSerializer = object : JsonSerializer<OrderType>() {
            override fun serialize(value: OrderType, gen: JsonGenerator, serializers: SerializerProvider?) {
                return gen.writeString(value.name.toLowerCase())
            }
        }

        simpleModule.addDeserializer(KucoinOrderBookUnit::class.java, orderBookUnitDeserializer)
        simpleModule.addDeserializer(KucoinAccount.KucoinAccountType::class.java, accountTypeDeserializer)
        simpleModule.addSerializer(CurrencyPair::class.java, currencyPairSerializer)
        simpleModule.addSerializer(TradeSideType::class.java, tradeSideTypeSerializer)
        simpleModule.addSerializer(OrderType::class.java, orderTypeSerializer)
    }
}
