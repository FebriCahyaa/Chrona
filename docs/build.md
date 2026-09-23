# Build

Chrona compiles against Android API 37.2 with a minimum supported Android version of API 26 and targetSdk 37. Debug and release builds produce five APKs: arm64-v8a, armeabi-v7a, x86, x86_64, and universal.

The Gradle build uses Android SDK Platform 37.2 and Build Tools 37.1.0. GitHub Actions installs and verifies the same platform and build-tools versions before running Gradle.
