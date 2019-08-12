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

package com.njkim.reactivecrypto.bhex

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.bhex.model.BhexOrderBookUnit
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BhexJsonObjectMapper : ExchangeJsonObjectMapper {
    companion object {
        val instance: ObjectMapper = BhexJsonObjectMapper().objectMapper()
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

    override fun currencyDeserializer(): JsonDeserializer<Currency>? {
        return object : JsonDeserializer<Currency>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Currency {
                return Currency.getInstance(p.valueAsString.toUpperCase())
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val orderBookUnitDeserializer = object : JsonDeserializer<BhexOrderBookUnit>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BhexOrderBookUnit {
                val jsonNode: JsonNode = p.codec.readTree(p)
                val price = instance.convertValue(jsonNode[0], BigDecimal::class.java)
                val quantity = instance.convertValue(jsonNode[1], BigDecimal::class.java)

                return BhexOrderBookUnit(price, quantity)
            }
        }

        simpleModule.addDeserializer(BhexOrderBookUnit::class.java, orderBookUnitDeserializer)
    }
}
