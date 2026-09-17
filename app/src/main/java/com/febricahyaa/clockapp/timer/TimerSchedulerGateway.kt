/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.timer

interface TimerSchedulerGateway {
    fun schedule(endAtEpochMillis: Long)
    fun cancel()
}
