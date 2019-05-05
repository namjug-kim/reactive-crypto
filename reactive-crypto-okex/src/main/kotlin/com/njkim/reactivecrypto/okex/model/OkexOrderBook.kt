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

package com.njkim.reactivecrypto.okex.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderBookUnit
import com.njkim.reactivecrypto.core.common.model.order.OrderSideType
import java.math.BigDecimal
import java.time.ZonedDateTime
import kotlin.streams.toList

/**
 * {
 * ”table”: "spot/trade”,
 * ”data ”: [
 * [
 * {
 * ”instrument_id”: "ETH-USDT”,
 * ”price”: "22888”,
 * ”side”: "buy”,
 * ”size”: "7”,
 * ”timestamp”: "2018-11-22T03:58:57.709Z”,
 * ”trade_id”: "108223090144493569”
 * }]
 * ]
 * }
 */
data class OkexOrderBookWrapper(
    @get:JsonProperty("table")
    val table: String,

    @get:JsonProperty("action")
    val action: String,

    @get:JsonProperty("data")
    val data: List<OkexOrderBook>
)

data class OkexOrderBook(
    @get:JsonProperty("instrument_id")
    val instrumentId: CurrencyPair,

    @JsonProperty("asks")
    private val asks: List<List<String>>,

    @JsonProperty("bids")
    private val bids: List<List<String>>,

    @get:JsonProperty("timestamp")
    val timestamp: ZonedDateTime,

    @get:JsonProperty("checksum")
    val checksum: String
) {
    fun getBids(): List<OrderBookUnit> {
        return bids.stream()
            .map { objects ->
                OrderBookUnit(
                    BigDecimal(objects[0]),
                    BigDecimal(objects[1]),
                    OrderSideType.BID,
                    objects[2].toInt()
                )
            }
            .toList()
    }

    fun getAsks(): List<OrderBookUnit> {
        return asks.stream()
            .map { objects ->
                OrderBookUnit(
                    BigDecimal(objects[0]),
                    BigDecimal(objects[1]),
                    OrderSideType.ASK,
                    objects[2].toInt()
                )
            }
            .toList()
    }
}