package com.njkim.reactivecrypto.core.common.model.account

import com.njkim.reactivecrypto.core.common.model.currency.Currency
import java.math.BigDecimal

data class Balance(
    val currency: Currency,
    val availableBalance: BigDecimal,
    val frozenBalance: BigDecimal
)