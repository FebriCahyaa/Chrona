/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.timer

interface TimerSchedulerGateway {
    fun schedule(endAtEpochMillis: Long)
    fun cancel()
}
