package com.febricahyaa.chrona.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class WeekdaysTest {

    @Test
    fun testFromBitsAndBitsProperty() {
        val weekdays = Weekdays.fromBits(0x05) // Monday (0x01) + Wednesday (0x04)
        assertEquals(0x05, weekdays.bits)
        assertTrue(weekdays.isBitOn(Calendar.MONDAY))
        assertFalse(weekdays.isBitOn(Calendar.TUESDAY))
        assertTrue(weekdays.isBitOn(Calendar.WEDNESDAY))
        assertFalse(weekdays.isBitOn(Calendar.THURSDAY))
        assertFalse(weekdays.isBitOn(Calendar.FRIDAY))
        assertFalse(weekdays.isBitOn(Calendar.SATURDAY))
        assertFalse(weekdays.isBitOn(Calendar.SUNDAY))
    }

    @Test
    fun testFromCalendarDays() {
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)
        assertEquals(0x11, weekdays.bits) // Monday (0x01) | Friday (0x10)
        assertTrue(weekdays.isBitOn(Calendar.MONDAY))
        assertTrue(weekdays.isBitOn(Calendar.FRIDAY))
        assertFalse(weekdays.isBitOn(Calendar.SUNDAY))
    }

    @Test
    fun testSetBit() {
        var weekdays = Weekdays.NONE
        assertFalse(weekdays.isBitOn(Calendar.MONDAY))

        weekdays = weekdays.setBit(Calendar.MONDAY, true)
        assertTrue(weekdays.isBitOn(Calendar.MONDAY))

        weekdays = weekdays.setBit(Calendar.MONDAY, false)
        assertFalse(weekdays.isBitOn(Calendar.MONDAY))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testIsBitOnInvalidDayThrowsException() {
        Weekdays.NONE.isBitOn(999)
    }

    @Test
    fun testIsRepeating() {
        assertFalse(Weekdays.NONE.isRepeating)
        assertTrue(Weekdays.ALL.isRepeating)
        assertTrue(Weekdays.fromBits(0x01).isRepeating)
    }

    @Test
    fun testGetDistanceToPreviousDay() {
        // Active days: Monday and Friday
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)

        val cal = Calendar.getInstance()

        // If today is Monday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        // Previous day should be Friday (3 days back: Mon -> Sun -> Sat -> Fri)
        assertEquals(3, weekdays.getDistanceToPreviousDay(cal))

        // If today is Tuesday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        // Previous day should be Monday (1 day back)
        assertEquals(1, weekdays.getDistanceToPreviousDay(cal))

        // If no days are set
        assertEquals(-1, Weekdays.NONE.getDistanceToPreviousDay(cal))
    }

    @Test
    fun testGetDistanceToNextDay() {
        // Active days: Monday and Friday
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)

        val cal = Calendar.getInstance()

        // If today is Monday (which is active)
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        assertEquals(0, weekdays.getDistanceToNextDay(cal))

        // If today is Tuesday
        cal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY)
        // Next active day is Friday (3 days ahead: Tue -> Wed -> Thu -> Fri)
        assertEquals(3, weekdays.getDistanceToNextDay(cal))

        // If no days are set
        assertEquals(-1, Weekdays.NONE.getDistanceToNextDay(cal))
    }

    @Test
    fun testEqualsAndHashCode() {
        val w1 = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.WEDNESDAY)
        val w2 = Weekdays.fromBits(0x05)
        val w3 = Weekdays.fromCalendarDays(Calendar.TUESDAY)

        assertEquals(w1, w2)
        assertEquals(w1.hashCode(), w2.hashCode())

        assertNotEquals(w1, w3)
        assertNotEquals(w1, null)
        assertNotEquals(w1, "String")
    }

    @Test
    fun testToStringRepresentation() {
        val weekdays = Weekdays.fromCalendarDays(Calendar.MONDAY, Calendar.FRIDAY)
        assertEquals("[M F]", weekdays.toString())
    }
}
