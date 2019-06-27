package com.njkim.reactivecrypto.core.common.model.order

/**
Created by jay on 27/06/2019
 **/
enum class OrderStatusType {
    WAIT, // 체결 대기
    DONE, // 전체 체결 완료
    CANCEL // 주문 취소
}