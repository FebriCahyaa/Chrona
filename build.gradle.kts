/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// CHRONA-SECURITY-RESOLUTION
// Centralized constraints keep patched transitive versions consistent across
// all configurations while leaving the direct dependency declarations in the
// app module version-catalog managed.
allprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            when (requested.group to requested.name) {
                "org.bitbucket.b_c" to "jose4j" -> useVersion(libs.versions.jose4j.get())
                "org.jdom" to "jdom2" -> useVersion(libs.versions.jdom2.get())
                "org.apache.httpcomponents" to "httpclient" -> useVersion(libs.versions.httpclient.get())
                "org.apache.commons" to "commons-lang3" -> useVersion(libs.versions.commonsLang3.get())
                "org.bouncycastle" to "bcpkix-jdk18on" -> useVersion(libs.versions.bouncycastle.get())
            }
        }
    }
}