package com.njkim.reactivecrypto.coineal.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

/**
Created by jay on 02/07/2019
 **/
data class CoinealBalanceWrapper(
    @JsonProperty("total_asset")
    val totalAsset: BigDecimal,
    @JsonProperty("coin_list")
    private val nullableCoinList: List<CoinealBalance>?
) {
    val coinList: List<CoinealBalance>
        get() {
            return nullableCoinList ?: emptyList()
        }
}