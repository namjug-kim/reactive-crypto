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

import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

/**
 * channelID	integer	ChannelID of pair-trade subscription
 * Array	array	Array of trades
 *  Array	array	Array of trade values
 *      price	    float	Price
 *      volume	    float	Volumeq
 *      time	    float	Time, seconds since epoch
 *      side	    string	Triggering order side (buy/sell), values: b|s
 *      orderType	string	Triggering order type (market/limit), values: m|l
 *      misc	    string	Miscellaneous
 */
data class KrakenTickDataWrapper(
    val channelId: Int,
    val data: List<KrakenTickData>
)

/**
 * @property price      Price
 * @property volume     Volume
 * @property time       Time, seconds since epoch
 * @property side       Triggering order side (buy/sell), values: b|s
 * @property orderType  Triggering order type (market/limit), values: m|l
 */
data class KrakenTickData(
    val time: ZonedDateTime,
    val price: BigDecimal,
    val volume: BigDecimal,
    val tradeSideType: TradeSideType,
    val orderType: String
)