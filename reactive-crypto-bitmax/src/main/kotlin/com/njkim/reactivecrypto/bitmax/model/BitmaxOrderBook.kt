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

package com.njkim.reactivecrypto.bitmax.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * @property seqnum         the sequence number is a strictly increasing integer number for each depth update assigned by the server.
 *                          In the message, it is the seqnum of the latest depth update.
 * @property ts             the UTC timestamp in milliseconds of the latest depth update contained in the message.
 * @property asks           ask levels, could be empty
 * @property bids           bid levels, could be empty
 *
 * @see BitmaxMessageFrame message frame default property
 */
data class BitmaxOrderBookWrapper(
    override val m: String,
    override val s: CurrencyPair,
    val ts: ZonedDateTime,
    val seqnum: Long,
    val asks: List<BitmaxOrderBook>,
    val bids: List<BitmaxOrderBook>
) : BitmaxMessageFrame(m, s)

/**
 * message : ["1.00669","0"]
 */
data class BitmaxOrderBook(
    val price: BigDecimal,
    val quantity: BigDecimal
)