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

import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.net.URLEncoder

fun Map<String, Any>.toMultiValueMap(): MultiValueMap<String, String> {
    val toMap = this
        .map { it.key to listOf("${it.value}") }
        .toMap()

    return LinkedMultiValueMap(toMap)
}

fun Map<String, Any>.toQueryString(): String {
    return this.entries
        .joinToString("&") { "${it.key}=${URLEncoder.encode("${it.value}", "UTF-8")}" }
}
