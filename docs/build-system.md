# Build System

Chrona keeps two build paths intentionally separate. `Android.bp` remains the AOSP/Soong integration path. The new Gradle project provides an independent local and GitHub Actions application build path without deleting or replacing Soong metadata.

The Gradle path targets Android API 36, uses JDK 17, Gradle 9.7.1, Android Gradle Plugin 9.4.0, Kotlin 2.4.20, and Material Components 1.14.0.

The root `gradlew` bootstrap uses the configured Gradle 9.7.1 distribution and honors `GRADLE_BIN` when a preinstalled Gradle binary is preferred. A stock Gradle wrapper can be regenerated locally with the same version when network access is available.
