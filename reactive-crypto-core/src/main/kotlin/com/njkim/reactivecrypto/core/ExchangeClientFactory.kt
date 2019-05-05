package com.njkim.reactivecrypto.core

import com.njkim.reactivecrypto.core.common.model.ExchangeVendor
import kotlin.reflect.full.createInstance

class ExchangeClientFactory {
    companion object {
        @JvmStatic
        fun getInstance(exchangeVendor: ExchangeVendor): ExchangeWebsocketClient {
            val websocketClientClass = Class.forName(exchangeVendor.implementedClassName)?.kotlin
            return websocketClientClass?.createInstance() as ExchangeWebsocketClient
        }
    }
}