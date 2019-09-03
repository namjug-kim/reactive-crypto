package com.njkim.reactivecrypto.kucoin.http

import com.njkim.reactivecrypto.core.http.AccountOperation
import com.njkim.reactivecrypto.core.http.OrderOperation
import com.njkim.reactivecrypto.core.http.PrivateHttpClient
import com.njkim.reactivecrypto.kucoin.http.raw.KucoinRawPrivateHttpClient

class KucoinPrivateHttpClient internal constructor(
    private val kucoinRawPrivateHttpClient: KucoinRawPrivateHttpClient
) : PrivateHttpClient("", "") {
    override fun account(): AccountOperation {
        return KucoinAccountOperator(kucoinRawPrivateHttpClient.account())
    }

    override fun order(): OrderOperation {
        return KucoinOrderOperator(kucoinRawPrivateHttpClient.trade())
    }
}
