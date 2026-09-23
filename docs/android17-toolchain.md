# Android 17 Toolchain

Chrona compiles against Android API 37.2 (Android 17.2 platform), keeps targetSdk at API 37, and supports devices from API 26 upward.

Toolchain contract:

- Android Gradle Plugin 9.4.1
- Gradle 9.7.1
- Kotlin 2.4.20
- JDK 17
- Android SDK Platform 37.2 (revision 1)
- Android SDK Build Tools 37.1.0

The Gradle configuration uses the AGP minor API DSL so `compileSdk` resolves to the installed `platforms;android-37.2` package instead of the obsolete `android-37` path.

The application currently has no native source tree, so ABI splits are produced by the Android Gradle Plugin packaging configuration without requiring an NDK install.

## Android 17 ABI outputs

Chrona targets Android API 37 with a minimum supported Android version of API 26. Debug and release builds produce five APKs: arm64-v8a, armeabi-v7a, x86, x86_64, and universal.

GitHub Actions installs and verifies the exact SDK components used by the Gradle build:

- `platforms;android-37.2`
- `build-tools;37.1.0`
