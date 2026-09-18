/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

import android.os.SystemClock

fun interface ChronaMonotonicClock {
    fun elapsedRealtime(): Long
}

val AndroidChronaMonotonicClock = ChronaMonotonicClock {
    SystemClock.elapsedRealtime()
}
