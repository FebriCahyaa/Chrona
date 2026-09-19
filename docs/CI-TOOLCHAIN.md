<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Chrona CI Android Toolchain

## Current source baseline

Chrona currently pins AGP 9.4.0, Gradle 9.7.1, compile/target SDK 37.1, Build Tools 37.0.0, NDK 28.2.13676358, and CMake 3.31.5. These versions should remain pinned in production CI.

## JDK strategy

- **JDK 25** is the CI Gradle runtime. Gradle 9.7.1 supports JVM 17 through 26 for running Gradle.
- **JDK 17** remains installed as the Kotlin/Android compilation toolchain because the project explicitly uses `jvmToolchain(17)` and AGP 9.4 documents JDK 17 as its minimum/default.
- **JDK 27** is the newest Java feature release as of September 2026, but it must not be used as the Gradle runtime for this repository while Gradle 9.7.1 remains in place because Gradle 9.7.1 does not officially support running on JVM 27.

This gives Chrona the newest production-appropriate LTS/runtime combination without making the build depend on an unsupported JVM.

## Android setup

- `android-actions/setup-android@v4.0.1` bootstraps the Android SDK Command-line Tools and licenses.
- Command-line tools **20.0 / build 14742923** are pinned for deterministic CI bootstrap.
- SDK package versions used by the project are installed explicitly rather than using floating `latest`.
- The new official **Android CLI** is installed in the toolchain audit and maintenance workflows from Google's stable package repository.

The Android CLI is intentionally separated from the normal build bootstrap. `setup-android` remains the reliable action for SDK initialization, while `android` is available for newer SDK management, device inspection, layout/screen inspection, and future journey-based QA.

## Gradle caching

Chrona uses `gradle/actions/setup-gradle@v6.2.0` with the **Basic Cache Provider**. This keeps Gradle caching in the workflow without depending on the optional proprietary enhanced cache provider.

## Planned CI capabilities

1. PR gate: static source audit, unit tests, lint, and debug assembly.
2. Toolchain audit: weekly verification of JDK/SDK/CLI/Gradle alignment.
3. Release: signed APK/AAB, apksigner verification, checksum, and GitHub Release.
4. Security: dependency review, CodeQL, secret scanning, SBOM, and artifact attestation.
5. Visual QA: Android CLI `layout`, `screen`, screenshots, and journey-style smoke tests once a device-based test path is reintroduced.

Instrumentation tests under `app/src/androidTest` remain available to a connected-device validation environment. The repository does not require a dedicated emulator script.

Do not change production build dependencies to dynamic versions such as `9.+`, `latest`, or an unpinned nightly. Keep future-version compatibility checks in separate scheduled workflows.
