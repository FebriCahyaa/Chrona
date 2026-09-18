/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.time

/** Pure policy used by the runtime ticker to decide whether foreground pulses are needed. */
object ChronaTickerEligibilityPolicy {
    fun shouldTick(foreground: Boolean, active: Boolean): Boolean = foreground && active
}
