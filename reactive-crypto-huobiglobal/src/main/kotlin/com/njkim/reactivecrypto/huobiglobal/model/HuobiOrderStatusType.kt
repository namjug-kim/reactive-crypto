package com.njkim.reactivecrypto.huobiglobal.model

import com.njkim.reactivecrypto.core.common.model.order.OrderStatusType

enum class HuobiOrderStatusType(private val rawString: String) {
    CREATED("created"),
    REJECTED("rejected"),
    TRIGGERED("triggered"),
    SUBMITTED("submitted"),
    PARTIAL_FILLED("partial-filled"),
    FILLED("filled"),
    PARTIAL_CANCELED("partial-canceled"),
    CANCELED("canceled");

    fun toOrderStatusType(): OrderStatusType {
        return when (this) {
            CREATED -> OrderStatusType.NEW
            REJECTED -> OrderStatusType.CANCELED
            TRIGGERED -> OrderStatusType.NEW
            SUBMITTED -> OrderStatusType.NEW
            PARTIAL_FILLED -> OrderStatusType.PARTIALLY_FILLED
            FILLED -> OrderStatusType.FILLED
            PARTIAL_CANCELED -> OrderStatusType.PARTIALLY_FILLED
            CANCELED -> OrderStatusType.CANCELED
        }
    }
}
