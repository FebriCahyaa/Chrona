# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

// Release signing is intentionally supplied by the CI environment rather than
// checked into the repository. Debug/local builds do not require release keys.
val releaseKeystorePath = providers.environmentVariable("CHRONA_KEYSTORE_PATH")
    .orNull
    ?.takeIf { it.isNotBlank() }
val releaseKeystorePassword = providers.environmentVariable("CHRONA_KEYSTORE_PASSWORD")
    .orNull
    ?.takeIf { it.isNotBlank() }
val releaseKeyAlias = providers.environmentVariable("CHRONA_KEY_ALIAS")
    .orNull
    ?.takeIf { it.isNotBlank() }
val releaseKeyPassword = providers.environmentVariable("CHRONA_KEY_PASSWORD")
    .orNull
    ?.takeIf { it.isNotBlank() }

val releaseSigningConfigured = listOf(
    releaseKeystorePath,
    releaseKeystorePassword,
    releaseKeyAlias,
    releaseKeyPassword,
).all { !it.isNullOrBlank() }

android {
    lint {
        abortOnError = true
        checkReleaseBuilds = true
        disable += "MissingTranslation"
    }
    namespace = "com.febricahyaa.clockapp"
    compileSdk = 37
    // Compose 1.13.0-alpha03 requires Android 37.1+ at compile time.
    compileSdkMinor = 1
    buildToolsVersion = "37.0.0"
    ndkVersion = "28.2.13676358"

    defaultConfig {
        applicationId = "com.febricahyaa.clockapp"
        minSdk = 26
        targetSdk = 37
        versionCode = 34
        versionName = "0.5.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-O2", "-ffast-math", "-fvisibility=hidden")
                arguments += listOf("-DANDROID_STL=c++_static")
            }
        }
    }

    signingConfigs {
        create("release") {
            if (releaseSigningConfigured) {
                storeFile = file(requireNotNull(releaseKeystorePath))
                storePassword = requireNotNull(releaseKeystorePassword)
                keyAlias = requireNotNull(releaseKeyAlias)
                keyPassword = requireNotNull(releaseKeyPassword)
            }
        }
    }

    buildTypes {
        release {
            // Keep the build configuration loadable for Debug/Test tasks. The
            // dedicated verification task below fails clearly when Release is
            // requested without the required CI signing environment.
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }

            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.31.5"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// This check is only attached to Release packaging tasks so that ordinary
// Debug/Test/Lint jobs remain independent of the private release keystore.
val verifyReleaseSigning = tasks.register("verifyReleaseSigning") {
    group = "verification"
    description = "Verify that the required Chrona release signing environment is configured."

    notCompatibleWithConfigurationCache(
        "Release signing verification currently captures Gradle script state."
    )

    doLast {
        if (!releaseSigningConfigured) {
            throw GradleException(
                "Release signing is not configured. Set CHRONA_KEYSTORE_PATH, " +
                    "CHRONA_KEYSTORE_PASSWORD, CHRONA_KEY_ALIAS, and CHRONA_KEY_PASSWORD."
            )
        }

        val keystore = file(requireNotNull(releaseKeystorePath))
        if (!keystore.isFile) {
            throw GradleException("Release keystore does not exist: ${keystore.absolutePath}")
        }
    }
}

tasks.matching {
    it.name == "assembleRelease" ||
        it.name == "bundleRelease" ||
        it.name == "packageRelease"
}.configureEach {
    dependsOn(verifyReleaseSigning)
}

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

dependencies {
    // CHRONA-SECURITY-CONSTRAINTS
    constraints { implementation(libs.jose4j) }
    constraints { implementation(libs.jdom2) }
    constraints { implementation(libs.httpclient) }
    constraints { implementation(libs.commons.lang3) }
    constraints { implementation(libs.bouncycastle.bcpkix) }

    implementation(platform(libs.compose.bom))
    androidTestImplementation(platform(libs.compose.bom))

    implementation(libs.core.ktx)
    implementation(libs.annotation)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.compose.animation)
    implementation(libs.compose.runtime.saveable)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.compose.adaptive)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    coreLibraryDesugaring(libs.desugar.jdk)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}