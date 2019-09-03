package com.njkim.reactivecrypto.kucoin.http

import com.njkim.reactivecrypto.core.common.util.CryptUtil
import com.njkim.reactivecrypto.core.common.util.toBase64String
import com.njkim.reactivecrypto.kucoin.KucoinJsonObjectMapper
import org.springframework.http.HttpMethod

object KucoinAuthUtil {
    fun sign(
        secretKey: String,
        method: HttpMethod,
        path: String,
        timestamp: Long,
        body: Map<String, Any> = emptyMap()
    ): String {
        val jsonString = when (method) {
            HttpMethod.GET -> ""
            else -> KucoinJsonObjectMapper.instance.writeValueAsString(body)
        }
        val stringToSign = "$timestamp$method$path$jsonString"

        return CryptUtil
            .encrypt("HmacSHA256", stringToSign.toByteArray(), secretKey.toByteArray())
            .toBase64String()
    }
}
