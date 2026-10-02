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

package com.febricahyaa.chrona

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class UtilsTest {

    @Test
    fun testIndexOf() {
        val array = arrayOf("apple", "banana", "cherry")
        assertEquals(0, Utils.indexOf(array, "apple"))
        assertEquals(1, Utils.indexOf(array, "banana"))
        assertEquals(2, Utils.indexOf(array, "cherry"))
        assertEquals(-1, Utils.indexOf(array, "durian"))
    }

    @Test
    fun testCalculateRadiusOffset() {
        val stroke = 10.0f
        val dot = 15.0f
        val marker = 12.0f

        val maxOffset = Utils.calculateRadiusOffset(stroke, dot, marker)
        assertEquals(15.0f, maxOffset, 0.001f)
    }

    @Test
    fun testGetGMTHourOffset() {
        val utcTz = TimeZone.getTimeZone("GMT")
        assertEquals("GMT +0:00", Utils.getGMTHourOffset(utcTz, false))
        assertEquals("+0", Utils.getGMTHourOffset(utcTz, true))

        val estTz = TimeZone.getTimeZone("GMT-5")
        assertEquals("GMT -5:00", Utils.getGMTHourOffset(estTz, false))
        assertEquals("-5", Utils.getGMTHourOffset(estTz, true))
    }
}
