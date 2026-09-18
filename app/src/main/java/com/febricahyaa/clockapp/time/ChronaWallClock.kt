/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

fun interface ChronaWallClock {
    fun epochMillis(): Long
}

val AndroidChronaWallClock = ChronaWallClock {
    System.currentTimeMillis()
}
