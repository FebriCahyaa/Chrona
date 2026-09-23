# Android 17 Toolchain

Chrona builds against Android 17 API 37 with minSdk 26 and targetSdk 37.

Toolchain contract:

- Android Gradle Plugin 9.4.1
- Gradle 9.7.1
- Kotlin 2.4.20
- JDK 17
- Android SDK Platform 37
- Android SDK Build Tools 37.0.0
The application currently has no native source tree, so ABI splits are produced by the Android Gradle Plugin packaging configuration without requiring an NDK install.
