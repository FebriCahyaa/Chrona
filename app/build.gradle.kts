/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
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
        versionCode = 32
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
    // Patched transitive versions reported by GitHub Dependency Graph.
    constraints { implementation("org.bitbucket.b_c:jose4j:0.9.6") }
    constraints { implementation("org.jdom:jdom2:2.0.6.1") }
    constraints { implementation("org.apache.httpcomponents:httpclient:4.5.13") }
    constraints { implementation("org.apache.commons:commons-lang3:3.18.0") }
    constraints { implementation("org.bouncycastle:bcpkix-jdk18on:1.84") }

    val composeBom = platform("androidx.compose:compose-bom-alpha:2026.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.19.0")
    implementation("androidx.annotation:annotation:1.9.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.10.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("androidx.work:work-runtime-ktx:2.11.2")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("com.squareup.retrofit2:retrofit:3.0.0")
    implementation("com.squareup.retrofit2:converter-gson:3.0.0")

    // Material 3 Expressive / Android 17 UI stack. The alpha BOM is intentional:
    // it exposes the latest Material 3 Expressive APIs (1.5.0-alpha28) and
    // Compose 1.13.0-alpha03 as of September 2026.
    implementation("androidx.compose.material3.adaptive:adaptive:1.4.0-alpha02")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")

    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.4.20")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
