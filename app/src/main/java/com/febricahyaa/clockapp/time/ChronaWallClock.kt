/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

fun interface ChronaWallClock {
    fun epochMillis(): Long
}

val AndroidChronaWallClock = ChronaWallClock {
    System.currentTimeMillis()
}
