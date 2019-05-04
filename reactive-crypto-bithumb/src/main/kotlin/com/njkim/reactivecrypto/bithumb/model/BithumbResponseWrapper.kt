package com.njkim.reactivecrypto.bithumb.model

/**
 * {
 * "amount":"0",
 * "data":[
 * {
 * "cont_no":34601963,
 * "price":"6632000",
 * "total":"952355.2",
 * "transaction_date":"2019-05-04 22:05:49.530989",
 * "type":"up",
 * "units_traded":"0.1436"
 * }
 * ],
 * "header":{
 * "currency":"BTC",
 * "service":"transaction"
 * },
 * "status":"0000"
 * }
 */
data class BithumbResponseWrapper<T>(
    val data: T,
    val header: BithumbResponseHeader,
    val status: String
)