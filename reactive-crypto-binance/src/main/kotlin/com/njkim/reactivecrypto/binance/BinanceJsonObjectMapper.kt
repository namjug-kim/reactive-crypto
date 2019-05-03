package com.njkim.reactivecrypto.binance

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.njkim.reactivecrypto.binance.BinanceCommonUtil.parseCurrencyPair
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BinanceJsonObjectMapper {
    private val log = KotlinLogging.logger {}

    companion object {
        val instance = BinanceJsonObjectMapper().objectMapper()
    }

    private fun objectMapper(): ObjectMapper {
        val simpleModule = SimpleModule()

        simpleModule.addDeserializer(ZonedDateTime::class.java, object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(p.longValue), ZoneId.systemDefault())
            }
        })
        simpleModule.addDeserializer(BigDecimal::class.java, object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal? {
                val valueAsString = p.valueAsString
                return if (StringUtils.isBlank(valueAsString)) {
                    null
                } else BigDecimal(valueAsString)
            }
        })
        simpleModule.addDeserializer(CurrencyPair::class.java, object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair? {
                val rawValue = p.valueAsString
                return parseCurrencyPair(rawValue)
            }
        })

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.registerModule(simpleModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }
}