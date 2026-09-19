/* Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. */

pluginManagement {
    repositories {
        google()
        maven {
            name = "MavenCentralCDN"
            url = uri("https://repo1.maven.org/maven2")
        }
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
    }
}

rootProject.name = "Chrona"
include(":app")
