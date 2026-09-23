
## Android 17 ABI outputs

Chrona targets Android 17 (API 37) with a minimum supported Android version of API 26. Debug and release builds produce four ABI-specific APKs: arm64-v8a, armeabi-v7a, x86, and x86_64. Universal APK generation is disabled.

The Gradle build uses Android SDK Platform 37 and Build Tools 37.0.0. GitHub Actions installs the same platform and build-tools versions before running Gradle.
