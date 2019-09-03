package com.njkim.reactivecrypto.kucoin.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.njkim.reactivecrypto.core.common.model.currency.Currency
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.OrderType
import com.njkim.reactivecrypto.core.common.model.order.TimeInForceType
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class KucoinOrder(
    @JsonProperty("cancelAfter")
    val cancelAfter: Int,
    @JsonProperty("cancelExist")
    val cancelExist: Boolean,
    @JsonProperty("channel")
    val channel: String,
    @JsonProperty("clientOid")
    val clientOid: String,
    @JsonProperty("createdAt")
    val createdAt: ZonedDateTime,
    @JsonProperty("dealFunds")
    val dealFunds: BigDecimal,
    @JsonProperty("dealSize")
    val dealSize: BigDecimal,
    @JsonProperty("fee")
    val fee: BigDecimal,
    @JsonProperty("feeCurrency")
    val feeCurrency: Currency,
    @JsonProperty("funds")
    val funds: BigDecimal,
    @JsonProperty("hidden")
    val hidden: Boolean,
    @JsonProperty("iceberg")
    val iceberg: Boolean,
    @JsonProperty("id")
    val id: String,
    @JsonProperty("isActive")
    val isActive: Boolean,
    @JsonProperty("opType")
    val opType: String,
    @JsonProperty("postOnly")
    val postOnly: Boolean,
    @JsonProperty("price")
    val price: BigDecimal,
    @JsonProperty("side")
    val side: TradeSideType,
    @JsonProperty("size")
    val size: BigDecimal,
    @JsonProperty("stop")
    val stop: String,
    @JsonProperty("stopPrice")
    val stopPrice: BigDecimal,
    @JsonProperty("stopTriggered")
    val stopTriggered: Boolean,
    @JsonProperty("stp")
    val stp: String,
    @JsonProperty("symbol")
    val symbol: CurrencyPair,
    @JsonProperty("timeInForce")
    val timeInForce: TimeInForceType,
    @JsonProperty("type")
    val type: OrderType,
    @JsonProperty("visibleSize")
    val visibleSize: BigDecimal
)
