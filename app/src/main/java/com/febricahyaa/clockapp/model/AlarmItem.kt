/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.model

import java.time.DayOfWeek
import java.time.LocalTime

/** Persistent alarm definition shared by UI, scheduler and ringing state. */
data class AlarmItem(
    val id: Long,
    val time: LocalTime,
    val label: String,
    val enabled: Boolean,
    val repeatDays: Set<DayOfWeek>,
    val ringtoneUri: String? = null,
    val ringtoneName: String = "",
    val vibrate: Boolean = true,
)
