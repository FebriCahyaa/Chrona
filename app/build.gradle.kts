/*
 * Copyright 2026 Febrian Rahmad Cahya
 * SPDX-License-Identifier: MIT
 */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

// Firebase/Google Services are applied only when a real configuration file is
// present (or explicitly enabled by CI). OSS builds therefore remain
// credential-free and reproducible.
val firebaseExplicitlyEnabled = providers.gradleProperty("chronaEnableFirebase")
    .map(String::toBoolean)
    .orElse(false)
    .get()
val firebaseConfigFile = file("google-services.json")
val firebaseEnabled = firebaseExplicitlyEnabled || firebaseConfigFile.isFile

if (firebaseEnabled) {
    if (!firebaseConfigFile.isFile) {
        throw GradleException(
            "Firebase is enabled but app/google-services.json is missing. " +
                "Provide the Play configuration or build with -PchronaEnableFirebase=false."
        )
    }
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

val chronaTargetAbi = providers.gradleProperty("chronaTargetAbi")
    .orNull
    ?.takeIf { it in setOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64") }

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
        testInstrumentationRunner = "com.febricahyaa.clockapp.ChronaTestRunner"
        manifestPlaceholders["chronaAppLinkHost"] = providers.gradleProperty("chronaAppLinkHost")
            .orElse("chrona.example.invalid")
            .get()
        buildConfigField("String", "CHRONA_DISTRIBUTION", "\"unknown\"")
        buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", firebaseEnabled.toString())
        ndk {
            chronaTargetAbi?.let { abiFilters.add(it) }
        }
        externalNativeBuild {
            cmake {
                cppFlags += listOf("-std=c++20", "-O2", "-ffast-math", "-fvisibility=hidden")
                arguments += listOf("-DANDROID_STL=c++_static")
            }
        }
    }

    flavorDimensions += "distribution"
    productFlavors {
        create("oss") {
            dimension = "distribution"
            applicationIdSuffix = ".oss"
            buildConfigField("String", "CHRONA_DISTRIBUTION", "\"oss\"")
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", "false")
        }
        create("play") {
            dimension = "distribution"
            buildConfigField("String", "CHRONA_DISTRIBUTION", "\"play\"")
            buildConfigField("Boolean", "CRASH_REPORTING_ENABLED", firebaseEnabled.toString())
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
            if (releaseSigningConfigured) {
                signingConfig = signingConfigs.getByName("release")
            }
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            ndk {
                debugSymbolLevel = "FULL"
            }
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
        aidl = true
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

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

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
    it.name in setOf(
        "assemblePlayRelease",
        "bundlePlayRelease",
        "packagePlayRelease",
        "assembleOssRelease",
        "bundleOssRelease",
        "packageOssRelease",
    )
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
    implementation(libs.hilt.android)
    implementation(libs.hilt.work)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.work.runtime.ktx)
    implementation(libs.metrics.performance)
    implementation(libs.lottie.compose)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.navigation.compose)
    implementation(libs.compose.animation)
    implementation(libs.compose.runtime.saveable)
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.compose.adaptive)
    implementation(libs.play.services.location)
    implementation(libs.locationbutton.compose)
    implementation(libs.compose.material.icons.core)
    implementation(libs.compose.material.icons.extended)

    ksp(libs.hilt.android.compiler)
    ksp(libs.room.compiler)
    ksp(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.android.compiler)
    kspAndroidTest(libs.hilt.compiler)

    "playImplementation"(platform(libs.firebase.bom))
    "playImplementation"(libs.firebase.crashlytics)
    "playImplementation"(libs.firebase.crashlytics.ndk)

    coreLibraryDesugaring(libs.desugar.jdk)

    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.room.testing)
    testImplementation(libs.mockk)

    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}
