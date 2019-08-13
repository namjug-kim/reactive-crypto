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

package com.njkim.reactivecrypto.bitz

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.bitz.model.BitzOrderBookUnit
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BitzJsonObjectMapper : ExchangeJsonObjectMapper {
    companion object {
        val instance: ObjectMapper = BitzJsonObjectMapper().objectMapper()
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

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return object : JsonDeserializer<TradeSideType>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return TradeSideType.valueOf(p.valueAsString.toUpperCase())
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

    override fun customConfiguration(simpleModule: SimpleModule) {
        val orderBookUnitDeserializer = object : JsonDeserializer<BitzOrderBookUnit>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BitzOrderBookUnit {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val price = instance.convertValue(jsonNode[0], BigDecimal::class.java)
                val quantity = instance.convertValue(jsonNode[1], BigDecimal::class.java)
                val totalAmount = instance.convertValue(jsonNode[2], BigDecimal::class.java)

                return BitzOrderBookUnit(price, quantity, totalAmount)
            }
        }

        simpleModule.addDeserializer(BitzOrderBookUnit::class.java, orderBookUnitDeserializer)
    }
}
