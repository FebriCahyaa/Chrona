/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.model.AlarmItem

/** Pure policy separating stale alarm events from explicit snooze events. */
object AlarmTriggerPolicy {
    fun shouldRing(alarm: AlarmItem?, isSnooze: Boolean): Boolean =
        isSnooze || alarm?.enabled == true
}
