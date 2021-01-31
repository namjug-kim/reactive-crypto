package com.njkim.reactivecrypto.huobikorea

import com.njkim.reactivecrypto.core.ExchangeClientFactory
import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import mu.KotlinLogging
import org.junit.Test

internal class HuobiKoreaPrivateWebsocketClientTest {

    private val log = KotlinLogging.logger {}

    @Test
    internal fun name() {
        val orderEvent = ExchangeClientFactory.privateWebsocket(ExchangeVendor.HUOBI_KOREA, "7922475c-df733244-5289092c-uymylwhfeg", "8a9bf5f6-0d2a6f1e-df392718-af123")
            .orderEvent()
            .doOnNext { log.info { it } }
            .blockLast()
    }
}
