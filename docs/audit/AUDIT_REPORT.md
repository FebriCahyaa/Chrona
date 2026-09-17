<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Chrona source-wide audit

Date: 2026-09-17

## Scope

Audited the Android source across Kotlin, Java, C++, JNI, Compose/Material imports, generated-resource references, manifest resources, CMake and tests.

## Source inventory

- 36 Kotlin source files
- 4 Java source files
- 2 C++ implementation files + 1 header
- Android resources and manifest references audited

## Checks completed

- Kotlin package/declaration and local-import consistency scan.
- Android resource reference scan (`R.string`, `R.drawable`, `R.layout`, `R.xml`, `R.mipmap`, `R.style`). No missing resource references detected.
- JNI Java/native method-name and descriptor alignment review.
- Native payload size checks for clock, solar and moon arrays.
- C++ host syntax check with C++20 and JNI headers.
- C++ warnings reviewed; unused native symbols removed.
- Java fallback calculations reviewed against native algorithms.
- Timer and stopwatch monotonic-time path reviewed.
- Compose Material icon dependency checked and made explicit.
- Accent gradient CompositionLocal wiring fixed so selected accent reaches UI components.
- Unit-test timezone assumption fixed so the midnight test is independent of the CI/device timezone.
- CI workflows, release signing, repository layout, and project Gradle configuration were reworked after the source refactor; the current declared toolchain is recorded under `docs/build/BUILD_ENVIRONMENT.md`.
- Kotlin ViewModel source compiles independently with the local Kotlin compiler using Android lifecycle/coroutines stubs for syntax and type-check coverage.
- Deterministic `AlarmTimeCalculator` checks pass for one-shot and repeating schedules in a fixed timezone.

## Build limitation

A full Gradle/Android compile could not be executed in this environment because the Gradle wrapper distribution (`gradle-9.6.1-bin.zip`) is not locally cached and `services.gradle.org` is unreachable from the execution environment. Therefore this report distinguishes static/native validation from an Android compiler build.

## Foundation lifecycle changes in this revision

- Added `AlarmService` and `AlarmStateManager` so alarm playback is no longer owned by a short-lived `BroadcastReceiver`.
- Added alarm recovery for boot, locale, time, timezone, package replacement, and exact-alarm permission-state changes.
- Added persistent Timer state plus `TimerReceiver`/`TimerService` expiry handling.
- Added persistent Stopwatch checkpoints using `SystemClock.elapsedRealtime()` and conservative reboot recovery.
- Persisted World Clock favorites.
- Added unit-testable `AlarmTimeCalculator` with deterministic time-zone tests.

## Remaining verification on a networked Android build host

Run:

```bash
./gradlew clean testDebugUnitTest assembleDebug
```

Then, for native diagnostics:

```bash
./gradlew :app:externalNativeBuildDebug
```

The Gradle 9.6.1 distribution itself is locally available for diagnostic use, but the Android Gradle Plugin and Android SDK/NDK dependency graph are not cached in this sandbox. An offline Gradle configuration attempt therefore stops at plugin resolution before Android compilation. Static source, XML, YAML, native syntax, Kotlin ViewModel type-checking, alarm-time checks, and copyright checks passed.

The CI configuration remains separated into Debug, Release, package-maintenance, and Dependabot workflows.
