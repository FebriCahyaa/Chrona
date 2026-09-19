/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.model.AlarmItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class AlarmTriggerPolicyTest {
    @Test
    fun disabledAlarmIsNotValidForNormalTrigger() {
        val alarm = AlarmItem(
            id = 1L,
            time = LocalTime.NOON,
            label = "Test alarm",
            enabled = false,
            repeatDays = emptySet(),
        )
        assertFalse(AlarmTriggerPolicy.shouldRing(alarm, isSnooze = false))
    }

    @Test
    fun snoozeCanRingAfterOriginalAlarmWasDisabled() {
        val alarm = AlarmItem(
            id = 1L,
            time = LocalTime.NOON,
            label = "Test alarm",
            enabled = false,
            repeatDays = emptySet(),
        )
        assertTrue(AlarmTriggerPolicy.shouldRing(alarm, isSnooze = true))
    }

    @Test
    fun missingAlarmCannotRingNormally() {
        assertFalse(AlarmTriggerPolicy.shouldRing(null, isSnooze = false))
        assertTrue(AlarmTriggerPolicy.shouldRing(null, isSnooze = true))
    }
}
