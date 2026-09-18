/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

package com.febricahyaa.clockapp.update

import com.febricahyaa.clockapp.data.update.GitHubReleaseRepository
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GitHubReleaseRepositoryTest {
    @Test
    fun versionTagAcceptsStandardReleaseForms() {
        assertTrue(GitHubReleaseRepository.isSemverLike("0.5.0"))
        assertTrue(GitHubReleaseRepository.isSemverLike("1.12.3-beta.1"))
        assertTrue(GitHubReleaseRepository.isSemverLike("2026.9.18"))
    }

    @Test
    fun versionTagRejectsNonVersionLabels() {
        assertFalse(GitHubReleaseRepository.isSemverLike("release-0.5.0"))
        assertFalse(GitHubReleaseRepository.isSemverLike("latest"))
        assertFalse(GitHubReleaseRepository.isSemverLike("0.x.1"))
    }

    @Test
    fun normalizeVersionRemovesReleasePrefixOnly() {
        assertTrue(GitHubReleaseRepository.normalizeVersion("v0.5.0") == "0.5.0")
        assertTrue(GitHubReleaseRepository.normalizeVersion(" V0.6.0 ") == "0.6.0")
    }
}
