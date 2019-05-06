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

package com.njkim.reactivecrypto.core

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * serialize/deserialize Exchange message
 *
 * @see AbstractExchangeWebsocketClient.createJsonObjectMapper
 */
interface ExchangeJsonObjectMapper {
    fun zonedDateTimeDeserializer(): JsonDeserializer<ZonedDateTime>? {
        return null
    }

    fun currencyPairDeserializer(): JsonDeserializer<CurrencyPair>? {
        return null
    }

    fun currencyDeserializer(): JsonDeserializer<Currency>? {
        return null
    }

    fun bigDecimalDeserializer(): JsonDeserializer<BigDecimal>? {
        return null
    }

    fun tradeSideTypeDeserializer(): JsonDeserializer<TradeSideType>? {
        return null
    }

    fun customConfiguration(simpleModule: SimpleModule) {
    }

    fun objectMapper(): ObjectMapper {
        val simpleModule = SimpleModule()

        zonedDateTimeDeserializer()?.let {
            simpleModule.addDeserializer(ZonedDateTime::class.java, it)
        }

        currencyDeserializer()?.let {
            simpleModule.addDeserializer(Currency::class.java, it)
        }

        currencyPairDeserializer()?.let {
            simpleModule.addDeserializer(CurrencyPair::class.java, it)
        }

        bigDecimalDeserializer()?.let {
            simpleModule.addDeserializer(BigDecimal::class.java, it)
        }

        tradeSideTypeDeserializer()?.let {
            simpleModule.addDeserializer(TradeSideType::class.java, it)
        }

        customConfiguration(simpleModule)

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.registerModule(simpleModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }

}