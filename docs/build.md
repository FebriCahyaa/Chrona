# Build

Chrona builds with Gradle (primary) and keeps `Android.bp` for AOSP/Soong
integration. Both build systems read the same sources under `app/src/main`.

## Toolchain

| Component | Version |
| --- | --- |
| Gradle (wrapper) | 9.7.1 |
| Android Gradle Plugin | 9.4.1 (built-in Kotlin) |
| Kotlin | 2.4.10 (below 2.4.20 for CodeQL compatibility) |
| JDK | 17 |
| minSdk / targetSdk | 29 / 37 |
| compileSdk / Build Tools | resolved per SDK channel (see below) |

SDK levels are defined once in `gradle/libs.versions.toml` (`androidCompileSdk`,
`androidTargetSdk`, `androidMinSdk`); CI can override compileSdk per channel with
`CHRONA_COMPILE_SDK`.

## Build types and SDK channels

Each build type compiles against a different `sdkmanager` channel. CI runs
`scripts/ci/android_sdk.py`, which lists the channel with
`sdkmanager --list --channel=N`, installs the newest platform and build-tools
available there, and exports them to Gradle.

| Build type | Branch | SDK channel | Application ID |
| --- | --- | --- | --- |
| `release` | `stable`, `v*` tags | `0` stable | `com.android.deskclock` |
| `debug` | `main`, pull requests | `1` beta | `com.android.deskclock.debug` |
| `dev` | `dev` | `2` dev | `com.android.deskclock.dev` |
| `canary` | `canary` | `3` canary | `com.android.deskclock.canary` |

A channel also contains every more stable channel's packages, so when a
channel has no preview package the build uses the newest stable one. The four
build types have different application IDs and can be installed side by side.

Gradle reads these environment variables (all optional locally):

| Variable | Meaning | Local default |
| --- | --- | --- |
| `CHRONA_COMPILE_SDK` | `37.2`, `37` or a preview codename | `37.2` |
| `CHRONA_BUILD_TOOLS` | Build Tools version | AGP default |
| `CHRONA_VERSION_CODE` | versionCode | `1` |
| `CHRONA_VERSION_NAME` | versionName | `1.0.0` |
| `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD` | release signing | unsigned |

## Local commands

```sh
./gradlew assembleDebug          # or assembleDev / assembleCanary / assembleRelease
./gradlew lintDebug testDebugUnitTest
./gradlew spotlessApply          # format Gradle scripts and config files
```

Every build type produces five APKs: `arm64-v8a`, `armeabi-v7a`, `x86`,
`x86_64` and `universal`.

To reproduce a CI channel locally (needs `sdkmanager` on `PATH`):

```sh
python3 scripts/ci/android_sdk.py --channel 2
```

## Repository layout

```text
app/                      Gradle application module
  build.gradle.kts
  lint.xml
  src/main/               AndroidManifest.xml, kotlin/, res/, assets/
  src/androidTest/java/   instrumentation tests
aosp/                     Soong-only files (generated BuildConfig, test manifest)
Android.bp                Soong modules DeskClock and DeskClockTests
gradle/                   wrapper and version catalog
scripts/ci/               CI helpers (SDK channels, versioning, packaging)
scripts/release/          signing key and changelog tooling
scripts/telegram/         Telegram notifier
docs/                     project documentation
```
