/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.update

import com.febricahyaa.clockapp.data.update.AppVersionComparator
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppVersionComparatorTest {
    @Test
    fun newerPatchIsDetected() {
        assertTrue(AppVersionComparator.isNewer("0.5.0", "0.5.1"))
    }

    @Test
    fun newerMinorIsDetected() {
        assertTrue(AppVersionComparator.isNewer("0.5.9", "0.6.0"))
    }

    @Test
    fun sameVersionIsNotNewer() {
        assertFalse(AppVersionComparator.isNewer("0.5.0", "0.5.0"))
    }

    @Test
    fun releasePrefixIsIgnored() {
        assertTrue(AppVersionComparator.isNewer("v0.5.0", "v0.5.2"))
    }

    @Test
    fun prereleaseSuffixIsIgnoredForStableLatestTagComparison() {
        assertTrue(AppVersionComparator.isNewer("0.5.0", "0.5.1-beta.1"))
    }
}
