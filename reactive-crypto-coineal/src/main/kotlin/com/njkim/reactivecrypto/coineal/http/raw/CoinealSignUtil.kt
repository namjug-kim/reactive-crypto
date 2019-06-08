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

@file:JvmName("CoinealSignUtil")

package com.njkim.reactivecrypto.coineal.http.raw

import com.fasterxml.jackson.module.kotlin.readValue
import com.njkim.reactivecrypto.coineal.CoinealJsonObjectMapper
import com.njkim.reactivecrypto.core.common.util.CryptUtil
import com.njkim.reactivecrypto.core.common.util.byteArrayToHex

fun sign(params: Map<String, Any>, secretKey: String): Map<String, Any> {
    val instance = CoinealJsonObjectMapper.instance
    val body = instance.writeValueAsString(params.toSortedMap(Comparator { o1, o2 -> o1.compareTo(o2) }))
    val readValue = instance.readValue<Map<String, Any>>(body)

    val signingPlainText = readValue
        .map { "${it.key}${it.value}" }
        .joinToString(separator = "", postfix = secretKey)

    val sign = CryptUtil
        .encrypt(
            "MD5",
            signingPlainText.toByteArray(Charsets.UTF_8)
        )
        .byteArrayToHex()
        .toLowerCase()

    val toMutableMap = readValue.toMutableMap()
    toMutableMap["sign"] = sign

    return toMutableMap
}