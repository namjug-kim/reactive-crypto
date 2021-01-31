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

package com.njkim.reactivecrypto.huobiglobal

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.module.SimpleModule
import com.njkim.reactivecrypto.core.ExchangeJsonObjectMapper
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import com.njkim.reactivecrypto.huobiglobal.model.HuobiOrderStatusType
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class HuobiJsonObjectMapper : ExchangeJsonObjectMapper {
    companion object {
        val instance = HuobiJsonObjectMapper()
    }

    override fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return object : JsonDeserializer<ZonedDateTime>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochMilli(p.longValue).atZone(ZoneId.systemDefault())
            }
        }
    }

    override fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair>? {
        return object : JsonDeserializer<CurrencyPair>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                return HuobiCommonUtil.parseCurrencyPair(p.valueAsString)
            }
        }
    }

    override fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return object : JsonDeserializer<TradeSideType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext?): TradeSideType {
                val rawData = p.valueAsString
                return if (rawData.contains("-")) {
                    val split = rawData.split("-")
                    TradeSideType.valueOf(split[0].toUpperCase())
                } else {
                    TradeSideType.valueOf(rawData.toUpperCase())
                }
            }
        }
    }

    override fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return object : JsonDeserializer<BigDecimal>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
                return BigDecimal(p.valueAsString)
            }
        }
    }

    override fun customConfiguration(simpleModule: SimpleModule) {
        val huobiOrderStatusTypeDeserializer = object : JsonDeserializer<HuobiOrderStatusType>() {
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): HuobiOrderStatusType {
                val rawOrderStatus = p.valueAsString

                return HuobiOrderStatusType.valueOf(
                    rawOrderStatus.toUpperCase().replace("-", "_")
                )
            }
        }

        simpleModule.addDeserializer(HuobiOrderStatusType::class.java, huobiOrderStatusTypeDeserializer)
    }
}
