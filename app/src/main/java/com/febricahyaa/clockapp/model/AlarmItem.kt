package com.febricahyaa.clockapp.model

import java.time.DayOfWeek
import java.time.LocalTime

/** A single user-created alarm. */
data class AlarmItem(
    val id: Long,
    val time: LocalTime,
    val label: String = "",
    val enabled: Boolean = true,
    val repeatDays: Set<DayOfWeek> = emptySet()
) {
    val isRepeating: Boolean get() = repeatDays.isNotEmpty()
}
