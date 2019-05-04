package com.njkim.reactivecrypto.bithumb

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import mu.KotlinLogging
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class BithumbJsonObjectMapper {
    private val log = KotlinLogging.logger {}

    companion object {
        val instance = BithumbJsonObjectMapper().objectMapper()
    }

    private fun objectMapper(): ObjectMapper {
        val simpleModule = SimpleModule()

        simpleModule.addDeserializer(ZonedDateTime::class.java, object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                // Bithumb use KOR(+9) timezone without zone information
                val parsedKorLocalDateTime = LocalDateTime.parse(
                    p.valueAsString,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS")
                )
                return ZonedDateTime.of(parsedKorLocalDateTime, ZoneOffset.ofHours(9))
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
        simpleModule.addDeserializer(Currency::class.java, object : JsonDeserializer<Currency>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Currency? {
                val rawValue = p.valueAsString
                return Currency.valueOf(rawValue)
            }
        })

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.registerModule(simpleModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }
}