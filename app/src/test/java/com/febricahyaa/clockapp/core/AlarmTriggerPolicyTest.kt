/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.core

import com.febricahyaa.clockapp.model.AlarmItem
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalTime

class AlarmTriggerPolicyTest {
    @Test
    fun disabledAlarmIsNotValidForNormalTrigger() {
        val alarm = AlarmItem(1L, LocalTime.NOON, enabled = false)
        assertFalse(AlarmTriggerPolicy.shouldRing(alarm, isSnooze = false))
    }

    @Test
    fun snoozeCanRingAfterOriginalAlarmWasDisabled() {
        val alarm = AlarmItem(1L, LocalTime.NOON, enabled = false)
        assertTrue(AlarmTriggerPolicy.shouldRing(alarm, isSnooze = true))
    }

    @Test
    fun missingAlarmCannotRingNormally() {
        assertFalse(AlarmTriggerPolicy.shouldRing(null, isSnooze = false))
        assertTrue(AlarmTriggerPolicy.shouldRing(null, isSnooze = true))
    }
}
