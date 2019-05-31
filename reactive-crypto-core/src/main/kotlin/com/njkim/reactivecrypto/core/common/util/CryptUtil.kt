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

package com.njkim.reactivecrypto.core.common.util

import java.security.MessageDigest
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import javax.xml.bind.DatatypeConverter

class CryptUtil {
    companion object {
        fun encrypt(algorithm: String, data: ByteArray, secretKey: ByteArray): ByteArray {
            val mac: Mac = Mac.getInstance(algorithm)
            val secKey = SecretKeySpec(secretKey, algorithm)
            mac.init(secKey)

            return mac.doFinal(data)
        }

        fun encrypt(algorithm: String, data: ByteArray): ByteArray {
            val mac: MessageDigest = MessageDigest.getInstance(algorithm)
            mac.update(data)

            return mac.digest()
        }
    }
}

fun ByteArray.toBase64String(): String = Base64.getEncoder().encodeToString(this)

fun String.hexToByteArray(): ByteArray = DatatypeConverter.parseHexBinary(this)

fun ByteArray.byteArrayToHex(): String = DatatypeConverter.printHexBinary(this)