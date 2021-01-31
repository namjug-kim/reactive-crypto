package com.njkim.reactivecrypto.huobiglobal.model

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import java.math.BigDecimal
import java.time.ZonedDateTime

data class HuobiTradeEventV2(
    val tradePrice: BigDecimal,
    val tradeVolume: BigDecimal,
    val tradeId: String,
    val tradeTime: ZonedDateTime,
    val aggressor: Boolean,
    val remainAmt: BigDecimal,
    val execAmr: BigDecimal,
    val orderId: String,
    val type: TradeSideType,
    val clientOrderId: String,
    val orderSource: String,
    val orderPrice: BigDecimal?,
    val orderSize: BigDecimal,
    val orderStatus: HuobiOrderStatusType,
    val symbol: CurrencyPair,
    val eventType: String
)
