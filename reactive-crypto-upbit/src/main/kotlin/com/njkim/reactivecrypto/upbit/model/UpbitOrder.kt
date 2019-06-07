package com.njkim.reactivecrypto.upbit.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import java.math.BigDecimal
import java.time.ZonedDateTime


/**
Created by jay on 07/06/2019
 **/
data class UpbitOrder(

    @get:JsonProperty("uuid")
    val uuid: String,

    @get:JsonProperty("side")
    val side: String,

    @get:JsonProperty("ord_type")
    val ordType: String,

    @get:JsonProperty("trade_date")
    val tradeDate: String,

    @get:JsonProperty("price")
    val price: BigDecimal,

    @get:JsonProperty("avg_price")
    val avgPrice: BigDecimal,

    @get:JsonProperty("market")
    val currencyPair: CurrencyPair,

    @get:JsonProperty("created_at")
    val createdAt: ZonedDateTime,

    @get:JsonProperty("volume")
    val volume: BigDecimal,

    @get:JsonProperty("remaining_volume")
    val remainingVolume: BigDecimal,

    @get:JsonProperty("reserved_fee")
    val reservedFee: BigDecimal,

    @get:JsonProperty("remaining_fee")
    val remainingFee: BigDecimal,

    @get:JsonProperty("paid_fee")
    val paidFee: BigDecimal,

    @get:JsonProperty("locked")
    val locked: BigDecimal,

    @get:JsonProperty("executed_volume")
    val executedVolume: BigDecimal,

    @get:JsonProperty("trade_count")
    val tradeCount: Int
)