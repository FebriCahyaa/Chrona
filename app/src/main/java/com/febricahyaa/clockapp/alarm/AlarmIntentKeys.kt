/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.alarm

/**
 * Intent extra keys shared by every alarm entry point: [AndroidAlarmScheduler]
 * (writer), [AlarmReceiver] / [AlarmActionReceiver] / [AlarmRingActivity]
 * (readers). Centralized here instead of being repeated as inline string
 * literals in each file, which is what invites typo-based bugs.
 */
object AlarmIntentKeys {
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_ALARM_HOUR = "extra_alarm_hour"
    const val EXTRA_ALARM_MINUTE = "extra_alarm_minute"
    const val EXTRA_ALARM_REPEAT_DAYS = "extra_alarm_repeat_days"
}
