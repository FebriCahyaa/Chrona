/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

import android.os.SystemClock

fun interface ChronaMonotonicClock {
    fun elapsedRealtime(): Long
}

val AndroidChronaMonotonicClock = ChronaMonotonicClock {
    SystemClock.elapsedRealtime()
}
