/*
 * Copyright 2019 namjug-kim
 *
 * LINE Corporation licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.njkim.reactivecrypto.core.netty

import com.njkim.reactivecrypto.core.common.exception.HeartBeatFailException
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

/**
 * @property pingTime ping message will be triggered when no write was performed for the specified
 * period of time.  Specify {@code 0} to disable.
 */
class HeartBeatHandler(
    observeOutput: Boolean,
    private val pingTime: Long,
    unit: TimeUnit,
    pongTimeout: Long,
    private val pingMessage: () -> String
) : IdleStateHandler(observeOutput, pingTime + pongTimeout, pingTime, 0, unit) {

    @Throws(Exception::class)
    override fun channelIdle(ctx: ChannelHandlerContext, evt: IdleStateEvent) {
        if (evt.state() == IdleState.READER_IDLE) {
            ctx.close()
            throw HeartBeatFailException("")
        } else if (evt.state() == IdleState.WRITER_IDLE || evt.state() == IdleState.ALL_IDLE) {
            ctx.channel().writeAndFlush(TextWebSocketFrame(pingMessage()))
        }
    }
}