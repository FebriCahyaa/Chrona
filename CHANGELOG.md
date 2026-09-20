<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Changelog

Chrona release notes describe changes that exist in the repository. Build,
test, lint, release and performance claims are recorded only when the
corresponding validation actually runs.

## Unreleased — Enterprise runtime foundation

- Added Hilt dependency injection across application ViewModels, repositories and Android entry points.
- Added Room for structured alarms/world-clock/history data and DataStore-backed durable settings/timer/stopwatch/update state.
- Added one-time legacy storage migration and an offline-first repository boundary.
- Added Hilt-enabled WorkManager maintenance workers for deferrable update/timezone health work.
- Added internal Binder/AIDL service boundary, Android Keystore secret storage, R8/ProGuard release rules and flavor-specific telemetry adapters.
- Added `oss`/`play` product flavors, MockK/JUnit unit coverage, Hilt instrumentation coverage, and Compose/Espresso smoke coverage.
- Added verified App Link/deep-link contract, optional Lottie adapter, and framework XML metadata for shortcuts/backup/network policy.
- Added timer full-screen completion activity, SystemUI countdown chronometer, exact completion scheduling, audio focus and haptic alerting; stopwatch foreground-service support remains separate from exact alarm scheduling.
- Preserved the existing native C++20/JNI/AAudio boundary without moving Android lifecycle responsibilities into C++.

## Unreleased — Deep source rebuild

### Build / Toolchain
- Migrated Hilt and Room annotation processing from KAPT to KSP.
- Upgraded Dagger/Hilt to 2.60.1 for stable KSP support.
- Added AndroidX Hilt compiler 1.4.0 for Hilt WorkManager code generation.
- Pinned KSP at 2.3.11 while retaining Kotlin 2.4.10.

### Architecture
- Consolidated the application around Kotlin, Coroutines/Flow, feature ViewModels, repository interfaces, Android platform gateways and a dedicated Java/JNI native boundary.
- Moved native bridge/facade sources into `app/src/main/java/com/febricahyaa/clockapp/nativelayer/`.
- Preserved the shared Chrona time engine and the existing alarm, timer, stopwatch, update and widget feature boundaries.

### World Clock
- Rebuilt the World Clock as a single Material 3 scrolling dashboard.
- Kept the first four favorites in a 2×2 grid and rendered remaining saved cities as full-width cards.
- Kept Current Location foreground-only and driven by Fused Location → LocationManager → recent last-known location → reverse geocoding.
- Changed location failures into transient Material 3 snackbar notifications instead of persistent dashboard placeholders.
- Rebuilt the fixed offline world map around device latitude/longitude without drag, pan or pinch gestures.
- Preserved the runtime Android ICU/IANA timezone catalog as the authority for timezone IDs and civil-time rules.

### Seconds display
- Added a persisted `SecondsDisplayMode` setting.
- Added `STACKED`, `INLINE`, `FADING_SCROLL`, `MINIMAL` and Material 3 `CIRCULAR` presentations.
- Added focused unit coverage for second progress and minute-cycle behavior.

### Android platform integration
- Retained `AlarmManager.setAlarmClock()` for exact alarm scheduling and the existing foreground-service/receiver recovery paths.
- Added/standardized `res/xml/` metadata for widget configuration, launcher shortcuts, network security and backup/data extraction rules.
- Kept Android `Vibrator`/`VibrationEffect` as the system haptic boundary and added an AAudio C++ fallback tone only when no usable alarm ringtone is available.

### Material 3
- Standardized feature UI on Material 3 components and tokens.
- Added a source audit that rejects Material 2 UI imports while allowing the standard Compose Material icon artifact.

### CI/CD
- Reduced GitHub Actions to exactly five workflow files:
  `update-commit.yml`, `debug-build.yml`, `release-build.yml`,
  `sync-source.yml` and `pull-request-issue.yml`.
- Added a four-ABI Debug compilation matrix.
- Made debug APK artifact upload explicit/manual rather than automatic on push.
- Made release packaging/publication manual-only.
- Folded security, dependency, localization, Telegram and maintenance checks
  into the five primary workflows.

### Documentation
- Reworked the README, repository structure, architecture, World Clock UI/data,
  CI/CD, release and Telegram documentation to describe the current source
  tree instead of phase/prototype artifacts.
- Removed obsolete active-repository audit/refactoring/phase documents.

## 0.5.0 — 2026-09-18

### Foundation
- Release built from the verified repository source.
- Release APK signature was checked with `apksigner`.
- SHA-256 checksum was generated for the release APK.

### Build metadata
- versionCode: `34`
- versionName: `0.5.0`

## Historical changes

Older release notes below are retained as historical context. They do not define
the current implementation contract; the current source tree and `AGENTS.md`
are authoritative.

### 2026-09-18 — Toolchain maintenance
- Bumped the checked-in Gradle wrapper to `9.7.1`.
- Standardized Android 17 / API 37 build tooling and native toolchain pins.
- Kept release signing outside the repository and supplied through GitHub Environment Secrets.

### Earlier foundation work
- Added the shared Chrona time engine with wall-clock and monotonic timing abstractions.
- Added persistent Alarm, Timer, Stopwatch and World Clock state.
- Added foreground alarm/timer service paths and reboot/timezone recovery receivers.
- Added dynamic timezone catalog behavior based on Android ICU/IANA runtime data.
- Added Compose Material 3 UI, adaptive settings layouts, localization audits and repository security/license checks.
