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

package com.njkim.reactivecrypto.upbit

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class UpbitJsonObjectMapper : ExchangeJsonObjectMapper {

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(p.longValue), ZoneId.systemDefault())
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair>? {
        return object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val rawValue = p.valueAsString
                val split = StringUtils.split(rawValue, "-")
                return CurrencyPair.parse(split[1], split[0])
            }
        }
    }

    /**
     * upbit value : ask, bid
     */
    override fun orderSideTypeDeserializer(): JsonDeserializer<OrderSideType>? {
        return object : JsonDeserializer<OrderSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): OrderSideType {
                val valueAsString = p.valueAsString
                return OrderSideType.valueOf(valueAsString.toUpperCase())
            }
        }
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal? {
                val valueAsString = p.valueAsString
                return if (StringUtils.isBlank(valueAsString)) {
                    null
                } else BigDecimal(valueAsString)
            }
        }
    }
}