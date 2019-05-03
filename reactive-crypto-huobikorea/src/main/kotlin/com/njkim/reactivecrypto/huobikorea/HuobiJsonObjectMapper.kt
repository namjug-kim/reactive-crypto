package com.njkim.reactivecrypto.huobikorea

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.IOException
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class HuobiJsonObjectMapper {
    companion object {
        val instance = HuobiJsonObjectMapper().objectMapper()
    }

    private fun objectMapper(): ObjectMapper {
        val simpleModule = SimpleModule()

        simpleModule.addDeserializer(ZonedDateTime::class.java, object : JsonDeserializer<ZonedDateTime>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): ZonedDateTime {
                return Instant.ofEpochMilli(p.longValue).atZone(ZoneId.systemDefault())
            }
        })

        simpleModule.addDeserializer(BigDecimal::class.java, object : JsonDeserializer<BigDecimal>() {
            @Throws(IOException::class, JsonProcessingException::class)
            override fun deserialize(p: JsonParser, ctxt: DeserializationContext): BigDecimal {
                return BigDecimal.valueOf(p.doubleValue)
            }
        })

        val objectMapper = ObjectMapper().registerKotlinModule()
        objectMapper.registerModule(simpleModule)
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }

}