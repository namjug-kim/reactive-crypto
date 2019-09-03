package com.njkim.reactivecrypto.kucoin.http.raw

import com.njkim.reactivecrypto.core.common.model.currency.CurrencyPair
import com.njkim.reactivecrypto.kucoin.model.KucoinOrderBookSnapshotResponse
import com.njkim.reactivecrypto.kucoin.model.KucoinResponseBody
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono

class KucoinRawMarketOperator internal constructor(
    private val webClient: WebClient
) {
    /**
     * Request via this endpoint to get the order book of the specified symbol.
     * Level 2 order book includes all bids and asks (aggregated by price). This level returns only one aggregated size for each price (as if there was only one single order for that price).
     * This API will return data with full depth.
     * It is generally used by professional traders because it uses more server resources and traffic, and we have strict access frequency control.
     * To maintain up-to-date Order Book, please use [com.njkim.reactivecrypto.kucoin.KucoinRawWebsocketClient] incremental feed after retrieving the Level 2 snapshot.
     */
    fun getFullOrderBook(symbol: CurrencyPair): Mono<KucoinOrderBookSnapshotResponse> {
        return webClient.get()
            .uri {
                it.path("/api/v2/market/orderbook/level2")
                    .queryParam("symbol", "${symbol.targetCurrency}-${symbol.baseCurrency}")
                    .build()
            }
            .retrieve()
            .bodyToMono<KucoinResponseBody<KucoinOrderBookSnapshotResponse>>()
            .map { it.data }
    }
}