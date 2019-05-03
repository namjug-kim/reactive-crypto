package com.njkim.reactivecrypto.core.common.model

enum class ExchangeVendor(val implementedClassName: String) {
    UPBIT("com.njkim.reactivecrypto.upbit.UpbitWebsocketClient"),
    BINANCE("com.njkim.reactivecrypto.binance.BinanceWebsocketClient"),
    HUOBI_KOREA("com.njkim.reactivecrypto.huobikorea.HuobiKoreaWebsocketClient");
}