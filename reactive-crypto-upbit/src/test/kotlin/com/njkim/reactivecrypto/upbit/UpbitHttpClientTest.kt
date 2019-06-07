package com.njkim.reactivecrypto.upbit

import com.njkim.reactivecrypto.core.ExchangeClientFactory
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.core.common.model.order.TradeSideType
import org.junit.Test
import java.math.BigDecimal
import java.util.concurrent.CountDownLatch


/**
Created by jay on 07/06/2019
 **/
class UpbitHttpClientTest {

    val accessKey = "[accessKey]"
    val secretKey = "[secretKey]"

    @Test
    fun balance() {
        val list = ExchangeClientFactory.http(ExchangeVendor.UPBIT)
            .privateApi(accessKey, secretKey)
            .account()
            .balance()

        for (b in list) {
            println(b)
        }

    }

    @Test
    fun marketOrder() {
        ExchangeClientFactory.http(ExchangeVendor.UPBIT)
            .privateApi(accessKey, secretKey)
            .order()
            .marketOrder(
                CurrencyPair.parse("BTC", "KRW"),
                TradeSideType.BUY,
                BigDecimal("100000"),
                BigDecimal("0")
            )
            .block()

        CountDownLatch(1).await()
    }

}