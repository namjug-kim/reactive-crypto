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

package com.njkim.reactivecrypto.kraken.model

import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * @property channelID ChannelID of pair-order book levels subscription
 * @property asks as : Array of price levels, ascending from best ask
 * @property bids bs : Array of price levels, descending from best bid
 * @property updateOnly is update payload. from check has property 'as', 'bs'
 *
 * @see com.njkim.reactivecrypto.kraken.KrakenJsonObjectMapper.customConfiguration jackson custom deserializer
 */
data class KrakenOrderBook(
    val channelID: Int,
    val asks: List<KrakenOrderBookUnit>,
    val bids: List<KrakenOrderBookUnit>,
    val isSnapshot: Boolean
)

/**
 * @property price PriceLevel
 * @property volume Price level volume
 * @property timestamp Price level last updated, seconds since epoch
 */
data class KrakenOrderBookUnit(
    val price: BigDecimal,
    val volume: BigDecimal,
    val timestamp: ZonedDateTime
)
