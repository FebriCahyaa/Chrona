/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.20")
    }
}
                }
            }
        }


plugins {
    id("com.android.application") version "9.4.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.20" apply false
}
// CHRONA-SECURITY-RESOLUTION
// These constraints apply to project configurations so patched transitive
// versions are selected when vulnerable versions are requested.
allprojects {
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            when (requested.group to requested.name) {
                "org.bitbucket.b_c" to "jose4j" -> useVersion("0.9.6")
                "org.jdom" to "jdom2" -> useVersion("2.0.6.1")
                "org.apache.httpcomponents" to "httpclient" -> useVersion("4.5.13")
                "org.apache.commons" to "commons-lang3" -> useVersion("3.18.0")
                "org.bouncycastle" to "bcpkix-jdk18on" -> useVersion("1.84")
            }
        }
    }
}

