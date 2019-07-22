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

package com.njkim.reactivecrypto.binance

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.binance.BinanceCommonUtil.parseCurrencyPair
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import mu.KotlinLogging
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BinanceJsonObjectMapper : ExchangeJsonObjectMapper {
    private val log = KotlinLogging.logger {}

    companion object {
        val instance = BinanceJsonObjectMapper().objectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(p.longValue), ZoneId.systemDefault())
            }
        }
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return object : JsonDeserializer<BigDecimal>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal? {
                val valueAsString = p.valueAsString
                return if (valueAsString.isBlank()) {
                    null
                } else {
                    BigDecimal(valueAsString)
                }
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair>? {
        return object : JsonDeserializer<CurrencyPair>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair? {
                val rawValue = p.valueAsString
                return parseCurrencyPair(rawValue)
            }
        }
    }

    override fun currencyDeserializer(): JsonDeserializer<Currency>? {
        return object : JsonDeserializer<Currency>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Currency? {
                val rawValue = p.valueAsString
                return Currency.getInstance(rawValue)
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val currencyPairSerializer = object : JsonSerializer<CurrencyPair>() {
            override fun serialize(value: CurrencyPair, gen: JsonGenerator, serializers: SerializerProvider?) {
                gen.writeString("${value.targetCurrency}${value.baseCurrency}")
            }
        }

        val bigDecimalSerializer = object : JsonSerializer<BigDecimal>() {
            override fun serialize(value: BigDecimal, gen: JsonGenerator, serializers: SerializerProvider?) {
                gen.writeString(value.toPlainString())
            }
        }

        val zonedDateTimeSerializer = object : JsonSerializer<ZonedDateTime>() {
            override fun serialize(value: ZonedDateTime, gen: JsonGenerator, serializers: SerializerProvider?) {
                gen.writeNumber(value.toEpochMilli())
            }
        }

        simpleModule.addSerializer(CurrencyPair::class.java, currencyPairSerializer)
        simpleModule.addSerializer(BigDecimal::class.java, bigDecimalSerializer)
        simpleModule.addSerializer(ZonedDateTime::class.java, zonedDateTimeSerializer)
    }
}
