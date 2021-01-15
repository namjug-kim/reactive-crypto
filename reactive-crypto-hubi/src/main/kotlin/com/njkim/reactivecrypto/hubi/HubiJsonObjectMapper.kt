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

package com.njkim.reactivecrypto.hubi

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.core.common.util.CurrencyPairUtil
import java.io.IOException
import java.math.BigDecimal
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatterBuilder
import java.util.*

class HubiJsonObjectMapper : ExchangeJsonObjectMapper {

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        val dateTimeFormatter = DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM dd, yyyy hh:mm:ss a")
            .toFormatter(Locale.ENGLISH)

        return object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return ZonedDateTime.parse(p.valueAsString, dateTimeFormatter.withZone(ZoneOffset.UTC))
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair> {
        return object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val rawCurrencyPair = p.valueAsString
                return CurrencyPairUtil.parse(rawCurrencyPair) ?: error("can't find currencyPair : $rawCurrencyPair")
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
                return TradeSideType.valueOf(valueAsString.toUpperCase())
            }
        }
    }
}
