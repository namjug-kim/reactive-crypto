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
@file:JvmName("UpbitSignUtil")

package com.njkim.reactivecrypto.upbit.http.raw

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.njkim.reactivecrypto.core.common.util.toQueryString
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

fun sign(params: Map<String, Any>, accessKey: String, secretKey: String): String {
    val algorithm = Algorithm.HMAC256(secretKey)

    val md = MessageDigest.getInstance("SHA-512")
    val queryString = params.toQueryString()
    md.update(queryString.toByteArray())

    val queryHash = String.format("%0128x", BigInteger(1, md.digest()))

    val builder = JWT.create()
        .withClaim("access_key", accessKey)
        .withClaim("nonce", UUID.randomUUID().toString())

    if (queryString.isNotEmpty()) {
        builder
            .withClaim("query_hash", queryHash)
            .withClaim("query_hash_alg", "SHA512")
    }

    return builder.sign(algorithm)
}
