<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Chrona

**Time, your way.**

Chrona is an Android clock application built with Jetpack Compose, Kotlin, Java, XML resources, and a small C++/JNI timing layer. The repository is organized around a maintainable application foundation and a reproducible CI/CD pipeline.

> The repository homepage intentionally contains only the information needed to understand, build, verify, and navigate the project. Detailed technical notes live in [`docs/`](docs/).

## Project status

The current repository is focused on the **Chrona foundation, architecture, and build/release infrastructure**. CI and runtime status are reported from actual workflow results rather than assumed from source configuration.

## What is in the app

| Area | Implementation |
| --- | --- |
| UI | Kotlin + Jetpack Compose + Material 3 Expressive + adaptive layouts |
| Features | Clock, World Clock, Timer, Stopwatch, Alarm, Settings |
| State | Dedicated feature ViewModels |
| Data | Repository interfaces + Android implementations |
| DI | Manual `AppContainer` composition root |
| Platform | Java time engine, Android receivers, widget provider |
| Native | C++20 timing/math layer through JNI |
| Resources | Android XML themes, strings, widget and adaptive icon resources |

## Architecture

```text
Compose UI
    │
    ▼
Feature ViewModels
    │
    ▼
Repositories / Gateways
    │
    ├── Android platform services
    │
    └── Java time + JNI bridge
                 │
                 ▼
             C++20 core
```

The native layer is deliberately small. Application state, navigation, persistence, and Android lifecycle behavior remain in Kotlin/Java.

## Build environment

The project declares an Android 17 / Jetpack Compose UI toolchain in source:

- JDK 17
- Android 17 (API 37)
- Android SDK Platform 37.0
- Android Build Tools 37.0.0
- NDK 28.2.13676358
- CMake 3.31.6
- Gradle 9.6.1
- Android Gradle Plugin 9.4.0
- Kotlin 2.4.20
- Jetpack Compose BOM 2026.09.00 (alpha channel for the latest Android 17-era Compose APIs)
- Material 3 Expressive 1.5.0-alpha28 via the Compose alpha BOM
- Material 3 Adaptive 1.4.0-alpha02 for window-aware layouts

See [`docs/build/BUILD_ENVIRONMENT.md`](docs/build/BUILD_ENVIRONMENT.md) for the authoritative project build notes.

## Local build

```bash
chmod +x ./gradlew
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Release builds require the release signing environment described in [`docs/release/RELEASE_SIGNING.md`](docs/release/RELEASE_SIGNING.md).

## CI / CD

GitHub Actions is intentionally separated by responsibility. Every Android build job installs and verifies the Android 17 SDK platform, Build Tools, NDK, and CMake toolchain before Gradle tasks run:

| Workflow | Purpose | Automatic? |
| --- | --- | --- |
| `debug-matrix.yml` | Debug audit, tests, lint, and APK build matrix | Yes |
| `release.yml` | Signed release APK, verification, checksum, changelog, GitHub Release | Manual |
| `package-maintenance.yml` | Build-tool/package maintenance checks | Scheduled / manual |
| `dependabot-auto-merge.yml` | Handles eligible Dependabot pull requests | Event-driven |

The release pipeline performs the following gates before publishing:

```text
version input
    ↓
tests + lint
    ↓
signed release APK
    ↓
apksigner verification
    ↓
SHA-256 checksum
    ↓
verified changelog entry
    ↓
repository version/changelog commit
    ↓
GitHub Release + artifacts
```

The release workflow uses the `release` GitHub Environment and never stores the private release keystore in the repository.

## Repository layout

```text
Chrona/
├── app/                     Android application source
├── .github/                 CI, Dependabot, issue/PR templates, ownership
├── docs/                    Architecture, build, audit, design, release notes
├── scripts/                 Repository maintenance scripts
├── gradle/                  Gradle wrapper files
├── build.gradle.kts         Root build configuration
├── settings.gradle.kts      Project/module configuration
├── gradle.properties        Gradle/Android properties
├── gradlew                  Gradle wrapper
├── gradlew.bat              Gradle wrapper for Windows
├── CHANGELOG.md             Verified release history
├── LICENSE                  Repository copyright terms
├── COPYRIGHT.md             Copyright and third-party notice
└── README.md                Repository overview
```

## Documentation

Start with [`docs/README.md`](docs/README.md) for the documentation map.

## Release policy

Chrona release notes must describe only changes supported by source inspection or successful CI results. Unverified features, fixes, performance claims, and test results are not added to release notes.

Release artifacts include the signed APK, SHA-256 checksum, and signature verification reports.

## Copyright

© 2026 Febrian Rahmad Cahya. All rights reserved.

See [`COPYRIGHT.md`](COPYRIGHT.md) and [`LICENSE`](LICENSE) for the repository copyright notice and third-party licensing boundary.
