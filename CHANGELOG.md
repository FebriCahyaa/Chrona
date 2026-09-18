## 0.10.1 — Edge-to-Edge / CI Resolution Reliability

- Fix `scripts/verify-android17.sh` to validate `ChronaBentoHomeScreen.kt` after the Dashboard rename.
- Accept the implemented `CircularProgressIndicator` as a valid timer progress surface; the verifier previously rejected the real implementation because it only looked for the wavy indicator or `drawArc`.
- Verify Dashboard edge-to-edge and collapsing-toolbar APIs explicitly (`enableEdgeToEdge`, `Scaffold`, `LargeTopAppBar`, `exitUntilCollapsedScrollBehavior`, and the nested-scroll connection).
- Remove the redundant legacy Kotlin `buildscript` classpath declaration so the Kotlin Gradle plugin is resolved only through the plugins DSL.
- Prefer Gradle 9.7.0, which is within the fully supported Gradle range documented for Kotlin 2.4.20, while remaining above AGP 9.4.0's minimum Gradle requirement.
- Add a transient Gradle dependency-resolution retry helper for Maven HTTP 429/5xx and common network failures.
- Limit Debug Matrix concurrency to two Gradle jobs at a time to reduce synchronized Maven Central requests.
- Keep application behavior unchanged; this release fixes verification/build infrastructure rather than timer/dashboard product behavior.

<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Changelog

Chrona release notes are evidence-based. Completed entries describe changes that exist in the repository source; build and release claims are added only after the corresponding CI gates succeed.

## Toolchain maintenance — 2026-09-18

- Bumped the checked-in Gradle wrapper to `9.7.1`.
- Replaced repository CI SDK package management with the official Android CLI (`android sdk`).
- Added scheduled package-maintenance source synchronization for Gradle and Android 17 toolchain pins.

## Unreleased — Phase 2 — Onboarding & Updates

- Added a first-run onboarding flow backed by Preferences DataStore, so completion persists transactionally across app restarts.
- Added a GitHub Releases updater for the public `FebriCahyaa/Chrona` repository using Retrofit 3.0.0.
- Added a lifecycle-safe update status surface in Settings with manual update checks and a release link when a newer version is available.
- Added a persistent WorkManager job that checks for releases every 24 hours when network connectivity is available, with exponential retry backoff.
- Added semantic version comparison and unit coverage for GitHub release tag handling.

### ⚙️ Build System

- Bumped application `versionName` to `0.5.0`.
- Bumped application `versionCode` from `31` to `32`.
- Added AndroidX DataStore Preferences `1.2.1`.
- Added WorkManager `2.11.2`.
- Added Retrofit `3.0.0` and Gson converter `3.0.0`.

### 🔐 Privacy & Control

- Onboarding state remains local to the device.
- Update checking reads the public GitHub Releases API; no GitHub account or access token is required.
- Chrona does not silently install APKs; the user opens the published release page explicitly.

### ⚠️ Validation

- Static source validation and patch checks are required before merge.
- Full Android compilation must be confirmed by the networked GitHub Actions build host.

## Unreleased — Production UI/UX Refactor

### 🎨 UI / UX

- Rebuilt Timer editing around a numeric 0–9 keypad with right-to-left `HHMMSS` entry; no `Slider`, Android `SeekBar`, or Chrona slider component remains in app source.
- Rebuilt running Timer visualization with Material 3 `CircularProgressIndicator`, 16dp stroke, rounded stroke caps, animated progress, and a lightweight `graphicsLayer` pulse.
- Rebuilt Alarm rows as expandable cards with `animateContentSize`, Material 3 switches/filter chips, ringtone selection through the Android system picker, vibration persistence, and delete actions.
- Added a dedicated World Clock city search screen using the current stateful Material 3 `SearchBar` API, plus a curated 25-city timezone catalog and live UTC offset presentation.
- Added tactile feedback to required actions through `LocalHapticFeedback`, including keypad input, buttons, filters, and toggles.

### ⚡ Compose / Performance

- Moved timer parsing/formatting into pure `TimerDurationInput` code and added unit coverage for HHMMSS input validation and serialization.
- Used stable `LazyColumn` keys and `contentType` for Alarm and World Clock lists.
- Kept World Clock ticking on a single screen-level epoch ticker instead of spawning a coroutine per row.
- Kept calculations and object construction for clock cards outside custom `DrawScope` rendering; Timer no longer uses a custom countdown Canvas.

### 🌐 System integration

- Preserved `INTERNET` and `ACCESS_NETWORK_STATE` so Chrona remains network-capable for online features.
- Retained the expanded Android notification-channel taxonomy and alarm/timer service architecture.

## Unreleased — Foundation / Build System

### 🏗️ Foundation

- Added `AlarmService` so alarm playback and foreground lifecycle no longer depend on a `BroadcastReceiver` remaining alive.
- Added `AlarmStateManager` for explicit one-shot disable and repeating-alarm rescheduling transitions.
- Extended alarm recovery to boot, locale, time, timezone, package replacement, and exact-alarm permission-state changes.
- Added persistent Timer state and a dedicated timer scheduler/service path.
- Added persistent Stopwatch state and recovery-safe elapsed-time checkpoints.
- Persisted World Clock favorites alongside the user's saved city list.
- Extracted alarm trigger-time calculation into a testable `AlarmTimeCalculator`.

### ⚙️ Build System

- Bumped the application to version `0.4.0` (`versionCode` 31).
- Updated the app to Android 17 API 37 for both `compileSdk` and `targetSdk`.
- Standardized Android SDK Build Tools to `37.0.0` across local project configuration and CI workflows.
- Kept Android Gradle Plugin `9.4.0` with Gradle `9.6.1`, the supported Android 17 build combination used by this project.
- Upgraded the Compose dependency line to `compose-bom-alpha:2026.09.00`, bringing the Android 17-era Compose stack and Material 3 Expressive `1.5.0-alpha28`.
- Enabled `MaterialExpressiveTheme` with expressive motion and increased shape scale for the Chrona UI.
- Replaced the timer wavy/seek interaction with a numeric HHMMSS keypad and a thick rounded circular countdown indicator.
- Added Material 3 Adaptive window APIs, explicit resizeable-activity support, and predictive-back readiness for modern Android windows.
- Added `scripts/verify-android17.sh`; Debug, package-maintenance, and Release CI now run the Android 17 toolchain verification gate.
- Kept Debug, test, lint, native, and Release responsibilities separated in GitHub Actions.
- Release signing remains isolated to the `release` GitHub Environment and is not stored in the repository.
- Release artifacts are designed to be verified with `apksigner` and accompanied by a SHA-256 checksum before publication.

### 🔐 Security

- Release keystore material remains outside the checked-out repository and is supplied through GitHub Environment Secrets.
- No production secret is introduced into the new alarm/timer/stopwatch lifecycle code.

### ⚠️ Known Issues

- Full Android compilation and device-level lifecycle testing still require a networked Android build host.
- Physical-device validation is still required for lock-screen alarm presentation, ringtone behavior, vendor background restrictions, and reboot/time-change edge cases.
