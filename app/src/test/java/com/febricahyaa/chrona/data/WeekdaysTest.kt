/*
 * Copyright (C) 2026 Chrona Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.febricahyaa.chrona.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class WeekdaysTest {

    @Test
    fun testFromBitsAndPredefinedConstants() {
        val none = Weekdays.NONE
        assertEquals(0, none.bits)
        assertFalse(none.isRepeating)

        val all = Weekdays.ALL
        assertEquals(0x7F, all.bits)
        assertTrue(all.isRepeating)
        assertEquals(7, all.count)
    }

    @Test
    fun testFromCalendarDays() {
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)
        // Monday = 0x01, Friday = 0x10 -> bits = 0x11
        assertEquals(0x11, weekdays.bits)
        assertTrue(weekdays.isBitOn(Calendar.MONDAY))
        assertTrue(weekdays.isBitOn(Calendar.FRIDAY))
        assertFalse(weekdays.isBitOn(Calendar.SUNDAY))
        assertEquals(2, weekdays.count)
    }

    @Test
    fun testSetBit() {
        var weekdays = Weekdays.NONE
        assertFalse(weekdays.isBitOn(Calendar.TUESDAY))

        weekdays = weekdays.setBit(Calendar.TUESDAY, true)
        assertTrue(weekdays.isBitOn(Calendar.TUESDAY))

        weekdays = weekdays.setBit(Calendar.TUESDAY, false)
        assertFalse(weekdays.isBitOn(Calendar.TUESDAY))
    }

    @Test
    fun testGetDistanceToNextDay() {
        // Active days: Monday and Wednesday
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.WEDNESDAY)

        val monday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val tuesday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY) }
        val thursday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY) }

        // On Monday, distance to next day is 0
        assertEquals(0, weekdays.getDistanceToNextDay(monday))
        // On Tuesday, distance to next day (Wednesday) is 1
        assertEquals(1, weekdays.getDistanceToNextDay(tuesday))
        // On Thursday, distance to next day (Monday) is 4
        assertEquals(4, weekdays.getDistanceToNextDay(thursday))
    }

    @Test
    fun testGetDistanceToNextDay_noneActive() {
        val weekdays = Weekdays.NONE
        val monday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        assertEquals(-1, weekdays.getDistanceToNextDay(monday))
    }

    @Test
    fun testGetDistanceToPreviousDay() {
        // Active days: Monday and Friday
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)

        val monday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.MONDAY) }
        val tuesday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY) }
        val sunday = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) }

        // On Tuesday, distance to previous day (Monday) is 1
        assertEquals(1, weekdays.getDistanceToPreviousDay(tuesday))
        // On Monday, distance to previous day (Friday) is 3
        assertEquals(3, weekdays.getDistanceToPreviousDay(monday))
        // On Sunday, distance to previous day (Friday) is 2
        assertEquals(2, weekdays.getDistanceToPreviousDay(sunday))
    }

    @Test
    fun testEqualsAndHashCode() {
        val w1 = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.WEDNESDAY)
        val w2 = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.WEDNESDAY)
        val w3 = Weekdays.fromCalendarDays(Calendar.SUNDAY)

        assertEquals(w1, w2)
        assertEquals(w1.hashCode(), w2.hashCode())
        assertNotEquals(w1, w3)
    }

    @Test
    fun testToStringFormatted() {
        val w = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
        assertEquals("[M W F]", w.toString())
    }
}
