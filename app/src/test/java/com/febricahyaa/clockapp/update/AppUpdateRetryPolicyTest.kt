/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

package com.febricahyaa.clockapp.update

import com.febricahyaa.clockapp.data.update.GitHubReleaseException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class AppUpdateRetryPolicyTest {
    @Test
    fun rateLimitIsRetried() {
        assertTrue(AppUpdateRetryPolicy.shouldRetry(GitHubReleaseException(429)))
    }

    @Test
    fun serverErrorIsRetried() {
        assertTrue(AppUpdateRetryPolicy.shouldRetry(GitHubReleaseException(503)))
    }

    @Test
    fun notFoundIsNotRetried() {
        assertFalse(AppUpdateRetryPolicy.shouldRetry(GitHubReleaseException(404)))
    }

    @Test
    fun accessDeniedIsNotRetried() {
        assertFalse(AppUpdateRetryPolicy.shouldRetry(GitHubReleaseException(403)))
    }

    @Test
    fun networkIoIsRetried() {
        assertTrue(AppUpdateRetryPolicy.shouldRetry(IOException("network")))
    }

    @Test
    fun programmingErrorIsNotRetried() {
        assertFalse(AppUpdateRetryPolicy.shouldRetry(IllegalStateException("bug")))
    }
}
