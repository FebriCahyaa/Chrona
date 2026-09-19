/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.update

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdateUrlValidationTest {

    private fun isValidWebUrl(rawUrl: String?): Boolean {
        val url = rawUrl?.trim()
        return !url.isNullOrBlank() && (url.startsWith("https://", ignoreCase = true) || url.startsWith("http://", ignoreCase = true))
    }

    @Test
    fun acceptsValidHttpAndHttpsUrls() {
        assertTrue(isValidWebUrl("https://github.com/FebriCahyaa/Chrona/releases/tag/v0.5.0"))
        assertTrue(isValidWebUrl("http://github.com/FebriCahyaa/Chrona/releases/tag/v0.5.0"))
        assertTrue(isValidWebUrl(" HTTPS://GITHUB.COM/RELEASE "))
    }

    @Test
    fun rejectsNonWebOrDangerousSchemes() {
        assertFalse(isValidWebUrl("file:///sdcard/malicious.apk"))
        assertFalse(isValidWebUrl("javascript:alert(1)"))
        assertFalse(isValidWebUrl("content://media/external/images/media"))
        assertFalse(isValidWebUrl("intent://custom_action#Intent;scheme=something;end"))
        assertFalse(isValidWebUrl(null))
        assertFalse(isValidWebUrl(""))
        assertFalse(isValidWebUrl("   "))
    }
}
