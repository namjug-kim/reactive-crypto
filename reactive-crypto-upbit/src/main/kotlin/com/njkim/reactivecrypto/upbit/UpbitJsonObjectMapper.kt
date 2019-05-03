package com.njkim.reactivecrypto.upbit

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class UpbitJsonObjectMapper {
    companion object {
        val instance = UpbitJsonObjectMapper().objectMapper()
    }

    private fun objectMapper(): ObjectMapper {
        val simpleModule = SimpleModule()
        simpleModule.addDeserializer(ZonedDateTime::class.java, object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return ZonedDateTime.ofInstant(Instant.ofEpochMilli(p.longValue), ZoneId.systemDefault())
            }
        })
        simpleModule.addDeserializer(BigDecimal::class.java, object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal? {
                val valueAsString = p.valueAsString
                return if (StringUtils.isBlank(valueAsString)) {
                    null
                } else BigDecimal(valueAsString)

            }
        })
        simpleModule.addDeserializer(CurrencyPair::class.java, object : JsonDeserializer<CurrencyPair>() {
            @Throws(IOException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): CurrencyPair {
                val rawValue = p.valueAsString
                val split = StringUtils.split(rawValue, "-")
                return CurrencyPair.parse(split[1], split[0])
            }
        })

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.registerModule(simpleModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }
}