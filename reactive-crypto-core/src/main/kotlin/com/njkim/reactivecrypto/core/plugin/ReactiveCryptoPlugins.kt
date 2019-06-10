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

package com.njkim.reactivecrypto.core.plugin

import com.njkim.reactivecrypto.core.plugin.strategy.CustomClientFactory
import java.util.concurrent.atomic.AtomicReference

object ReactiveCryptoPlugins {
    private val customClientFactoryReference: AtomicReference<CustomClientFactory> = AtomicReference()
    val customClientFactory: CustomClientFactory
        get() {
            val customClientFactory: CustomClientFactory? = customClientFactoryReference.get()

            if (customClientFactory == null) {
                this.customClientFactoryReference.compareAndSet(null, CustomClientFactory())
            }

            return this.customClientFactoryReference.get()
        }
}
