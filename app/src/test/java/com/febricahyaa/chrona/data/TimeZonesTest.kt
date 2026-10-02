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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TimeZonesTest {

    @Test
    fun testGetTimeZoneNameAndContains() {
        val ids = arrayOf<CharSequence>("America/New_York", "Europe/London", "Asia/Jakarta")
        val names = arrayOf<CharSequence>("New York", "London", "Jakarta")

        val timeZones = TimeZones(ids, names)

        assertEquals("New York", timeZones.getTimeZoneName("America/New_York"))
        assertEquals("London", timeZones.getTimeZoneName("Europe/London"))
        assertEquals("Jakarta", timeZones.getTimeZoneName("Asia/Jakarta"))

        assertNull(timeZones.getTimeZoneName("Unknown/Zone"))

        assertTrue(timeZones.contains("America/New_York"))
        assertFalse(timeZones.contains("Unknown/Zone"))
        assertFalse(timeZones.contains(null))
    }
}
