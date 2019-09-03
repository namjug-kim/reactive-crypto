package com.njkim.reactivecrypto.kucoin.http

import com.njkim.reactivecrypto.core.common.model.account.Balance
import com.njkim.reactivecrypto.core.http.AccountOperation
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawAccountOperator
import com.njkim.reactivecrypto.kucoin.model.KucoinAccount
import reactor.core.publisher.Flux

class KucoinAccountOperator internal constructor(
    private val rawAccountOperator: KucoinRawAccountOperator
) : AccountOperation("", "") {
    override fun balance(): Flux<Balance> {
        return rawAccountOperator
            .getAccounts()
            .filter { it.type == KucoinAccount.KucoinAccountType.TRADE }
            .map {
                Balance(
                    it.currency,
                    it.available,
                    it.holds
                )
            }
    }
}
