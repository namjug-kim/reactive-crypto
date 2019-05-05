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

package com.njkim.reactivecrypto.bithumb.model

/**
 * {
 * "amount":"0",
 * "data":[
 * {
 * "cont_no":34601963,
 * "price":"6632000",
 * "total":"952355.2",
 * "transaction_date":"2019-05-04 22:05:49.530989",
 * "type":"up",
 * "units_traded":"0.1436"
 * }
 * ],
 * "header":{
 * "currency":"BTC",
 * "service":"transaction"
 * },
 * "status":"0000"
 * }
 */
data class BithumbResponseWrapper<T>(
    val data: T,
    val header: BithumbResponseHeader,
    val status: String
)