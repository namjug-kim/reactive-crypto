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

package com.njkim.reactivecrypto.okexkorea

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.convertValue
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.okexkorea.model.OkexKoreaTickData
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class OkexKoreaJsonObjectMapper : ExchangeJsonObjectMapper {
    companion object {
        val instance: ObjectMapper = OkexKoreaJsonObjectMapper().objectMapper()
    }

    override fun currencyDeserializer(): JsonDeserializer<Currency>? {
        return object : JsonDeserializer<Currency>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Currency {
                val valueAsString = p.valueAsString
                return Currency.valueOf(valueAsString.toUpperCase())
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return object : JsonDeserializer<TradeSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): TradeSideType {
                return when (p.valueAsString) {
                    "bid" -> TradeSideType.BUY
                    "ask" -> TradeSideType.SELL
                    else -> throw IllegalArgumentException()
                }
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val localTimeDeserializer = object : JsonDeserializer<LocalTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): LocalTime {
                return LocalTime.parse(p.text, DateTimeFormatter.ISO_LOCAL_TIME)
            }
        }

        val tickDataDeserializer = object : JsonDeserializer<OkexKoreaTickData>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): OkexKoreaTickData {
                val jsonNode: JsonNode = p.codec.readTree(p)

                return OkexKoreaTickData(
                    jsonNode.get(0).asText(),
                    instance.convertValue(jsonNode.get(1)),
                    instance.convertValue(jsonNode.get(2)),
                    instance.convertValue(jsonNode.get(3)),
                    instance.convertValue(jsonNode.get(4))
                )
            }
        }

        simpleModule.addDeserializer(LocalTime::class.java, localTimeDeserializer)
        simpleModule.addDeserializer(OkexKoreaTickData::class.java, tickDataDeserializer)
    }
}
