# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

pluginManagement {
    repositories {
        google()
        maven {
            name = "MavenCentralCDN"
            url = uri("https://repo1.maven.org/maven2")
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        maven {
            name = "MavenCentralCDN"
            url = uri("https://repo1.maven.org/maven2")
        }
        mavenCentral()
    }
}

rootProject.name = "Chrona"
include(":app")