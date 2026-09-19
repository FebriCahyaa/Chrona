/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.time

/** Pure policy used by the runtime ticker to decide whether foreground pulses are needed. */
object ChronaTickerEligibilityPolicy {
    fun shouldTick(foreground: Boolean, active: Boolean): Boolean = foreground && active
}
