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

package com.njkim.reactivecrypto.upbit.http.raw

import com.njkim.reactivecrypto.upbit.model.UpbitApiException
import com.njkim.reactivecrypto.upbit.model.UpbitErrorResponse
import org.springframework.web.reactive.function.client.WebClient

fun WebClient.ResponseSpec.upbitErrorHandling(): WebClient.ResponseSpec = onStatus({ it.isError }, { clientResponse ->
    clientResponse.bodyToMono(UpbitErrorResponse::class.java)
        .map { UpbitApiException(clientResponse.statusCode(), it) }
})
