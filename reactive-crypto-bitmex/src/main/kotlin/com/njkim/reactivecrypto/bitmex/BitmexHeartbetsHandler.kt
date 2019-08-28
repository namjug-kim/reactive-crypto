package com.njkim.reactivecrypto.bitmex

import com.njkim.reactivecrypto.core.common.exception.HeartBeatFailException
import com.njkim.reactivecrypto.core.common.util.toEpochMilli
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

/**
 * If you are concerned about your connection silently dropping, we recommend implementing the following flow:
 *
 * - After receiving each message, set a timer a duration of 5 seconds.
 * - If any message is received before that timer fires, restart the timer.
 * - When the timer fires (no messages received in 5 seconds), send a raw ping frame (if supported) or the literal string 'ping'.
 * - Expect a raw pong frame or the literal string 'pong' in response. If this is not received within 5 seconds, throw an error or reconnect.
 */
class BitmexHeartbetsHandler(
    private val readIdleTime: Duration,
    private val pingMessage: String = "ping",
    private val pongMessage: String = "pong"

) : IdleStateHandler(false, readIdleTime.toMillis(), 0, 0, TimeUnit.MILLISECONDS) {
    private var lastPingDateTime: ZonedDateTime = ZonedDateTime.now()
    private var lastPongDateTime: ZonedDateTime = ZonedDateTime.now()

    @Throws(Exception::class)
    override fun channelIdle(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
        if (evt.state() == IdleState.READER_IDLE) {
            if (lastPongDateTime.toEpochMilli() - lastPingDateTime.toEpochMilli() >= readIdleTime.toMillis()) {
                ctx.close()
                throw HeartBeatFailException("")
            }

            lastPingDateTime = ZonedDateTime.now()
            ctx.channel().writeAndFlush(TextWebSocketFrame(pingMessage))
        }
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        if (msg is TextWebSocketFrame && msg.text() == pongMessage) {
            lastPongDateTime = ZonedDateTime.now()
        }
        super.channelRead(ctx, msg)
    }
}
