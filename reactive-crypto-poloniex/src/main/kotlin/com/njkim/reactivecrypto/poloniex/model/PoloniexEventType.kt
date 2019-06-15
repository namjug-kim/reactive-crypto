package com.njkim.reactivecrypto.poloniex.model

enum class PoloniexEventType(
    val eventType: String,
    val classType: Class<out PoloniexEvent>
) {
    ORDER_BOOK_SNAPSHOT("i", PoloniexOrderBookSnapshotEvent::class.java),
    ORDER_BOOK_UPDATE("o", PoloniexOrderBookUpdateEvent::class.java),
    TRADE("t", PoloniexTradeEvent::class.java);

    companion object {
        private val poloniexEventTypeMap: Map<String, PoloniexEventType> = createPoloniexEventTypeMap()

        private fun createPoloniexEventTypeMap(): Map<String, PoloniexEventType> {
            return values()
                .map { it.eventType to it }
                .toMap()
        }

        /**
         * @param eventType poloniex websocket message frame eventType(i, o, t ...)
         */
        fun parse(eventType: String): PoloniexEventType {
            return poloniexEventTypeMap[eventType] ?: throw IllegalArgumentException()
        }
    }
}
