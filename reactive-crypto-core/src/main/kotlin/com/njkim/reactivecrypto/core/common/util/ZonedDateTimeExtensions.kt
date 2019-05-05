package com.njkim.reactivecrypto.core.common.util

import java.time.ZonedDateTime

fun ZonedDateTime.toEpochMilli(): Long = this.toInstant().toEpochMilli()