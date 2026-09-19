<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Changelog

## Unreleased — CI pipeline restructure and app icon refresh

- Restructured `ci.yml` into a staged pipeline: `analyze` → `verify` (matrix: unit tests, lint) → `debug-build` → `ci-status` aggregate gate. Existing job names (`AES / Analyze`, `AES / Unit tests`, `AES / Lint`, `AES / Debug APK`) are preserved.
- Ordered `security.yml` so the cheap license audit gates the CodeQL build, and added a `security-status` aggregate gate that tolerates event-dependent skipped jobs.
- Split `release.yml` into `tag` → `build` → `attest` → `publish` jobs with per-job least-privilege permissions; signing secrets are confined to the build job and artifacts are handed over via `upload-artifact`/`download-artifact`.
- Removed the Device QA emulator workflow (`device-qa.yml`), `scripts/ci/device-smoke.sh`, the `install-emulator` option and the emulator-only `platforms;android-37.0` package from the shared toolchain action, and Device QA references from Telegram notifications, README and docs. Local instrumentation via `scripts/dev/verify.sh full` is unchanged.
- Anchored the `release/` ignore rule to the repository root so `scripts/release/` is no longer ignored by Git.
- Refreshed the app icon (indigo → violet gradient dial with 10:10 hands, coral second hand, Themed Icons monochrome layer) as `ic_chrona_*` resources, with a single `mipmap-anydpi-v26` definition and regenerated legacy PNG fallbacks.
- Added `ic_stat_chrona` as a dedicated 24dp alpha-only notification small icon for alarm and timer notifications.

## Unreleased — Repository Engineering Hardening

- Reorganized active scripts into audit, CI, development, localization, release, Telegram, and World Clock diagnostic domains.
- Removed proven-dead UI/source classes and superseded CI verifier scripts; preserved historical phase records under `docs/archive/`.
- Added repository-wide copyright, dependency-license, resource, localization, and dynamic-timezone audit gates.
- Added the Chrona Android composite toolchain setup for JDK 25 Gradle runtime, JDK 17 compilation toolchain, pinned Android SDK packages, and optional native/emulator components.
- Added separate Telegram notification bots for CI, ordinary pull requests, Dependabot, and releases with non-overlapping templates.
- Added Crowdin localization synchronization and a documented expansion policy for additional languages/locales.
- Kept World Clock data dynamic from Android ICU/IANA runtime data instead of vendoring a frozen timezone database.
- Rebuilt GitHub Actions around least-privilege permissions, scheduled maintenance, security scanning, device QA, release provenance, and deterministic artifacts.
- Added a single local `scripts/dev/verify.sh` entrypoint for repeatable repository verification.

### Verification — current source audit

- Repository source/resource/license/timezone/localization audits pass in the offline analysis environment.
- GitHub Actions YAML parses successfully.
- Telegram renderer tests pass.
- Full Gradle execution remains dependent on downloading Gradle 9.7.1 from the configured distribution service on a networked runner.

## Unreleased — Phase 5A — Unified Time Engine Hardening

- Added an injectable wall-clock source and application-scoped shared wall-clock StateFlow.
- Exposed monotonic and wall-clock reads through the shared ChronaTimeEngine.
- Replaced independent foreground clock polling loops with the shared engine flow.
- Changed active engine refresh cadence to 50ms for stopwatch and 100ms for timer; wall-clock UI projection refreshes at 250ms.
- Removed one-second fixed-delay timing loops from foreground engine/UI paths.
- Migrated Timer and Stopwatch ViewModels to the shared engine clock APIs.
- Added focused ChronaTimeEngine smoke/unit coverage for monotonic elapsed and timer boundary behavior.

## 0.5.0 — 2026-09-18

### 🏗️ Foundation
- Release built from the verified repository source.

### ⚙️ Build System
- Release APK assembled by GitHub Actions.
- Release checks completed before publication.

### 🔐 Security
- APK signature verified with apksigner.
- SHA-256 checksum generated for the APK artifact.

### 📦 Build Metadata
- Commit: `6f43c9be2fc012dbd82d90324c74560f6f0806ab`
- versionCode: `34`
- SHA-256: `4f11bd56156730c320ab56a819fcbf918422c46581a7b44d53ec483678c7a613`

### ⚠️ Known Issues
- Only issues confirmed by CI or documented source audit belong here.
## 0.6.0 — Production Hardening / UX Integration

- Harden onboarding persistence with a dedicated notification-permission prompt marker and idempotent completion handling.
- Add onboarding system-back handling and a real Skip action.
- Prevent the background GitHub updater from performing release checks before onboarding has completed.
- Serialize GitHub release requests so foreground and WorkManager checks cannot overlap on the shared repository instance.
- Validate GitHub release tags before accepting release metadata.
- Distinguish transient GitHub failures (HTTP 429/5xx and network I/O) from terminal HTTP failures such as 403/404 for WorkManager retry behavior.
- Surface actionable updater errors in Settings instead of a generic failure string.
- Refactor Settings into focused appearance, clock, notification, update, and about sections while preserving the existing public Settings API.
- Add notification permission status and a direct route to Android notification settings.
- Bump the application to version `0.6.0` (`versionCode` 33).

## 0.10.1 — Edge-to-Edge / CI Resolution Reliability

- Fix `scripts/ci/verify-android17.sh` to validate `ChronaBentoHomeScreen.kt` after the Dashboard rename.
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
- Added `scripts/ci/verify-android17.sh`; Debug, package-maintenance, and Release CI now run the Android 17 toolchain verification gate.
- Kept Debug, test, lint, native, and Release responsibilities separated in GitHub Actions.
- Release signing remains isolated to the `release` GitHub Environment and is not stored in the repository.
- Release artifacts are designed to be verified with `apksigner` and accompanied by a SHA-256 checksum before publication.

### 🔐 Security

- Release keystore material remains outside the checked-out repository and is supplied through GitHub Environment Secrets.
- No production secret is introduced into the new alarm/timer/stopwatch lifecycle code.

### ⚠️ Known Issues

- Full Android compilation and device-level lifecycle testing still require a networked Android build host.
- Physical-device validation is still required for lock-screen alarm presentation, ringtone behavior, vendor background restrictions, and reboot/time-change edge cases.
