# Chrona — Agent Operating Manual

This repository operating manual applies to all AI engineering agents working on Chrona.

## 1. Mission Statement

Act as a senior Android engineer working directly on the Chrona repository (`FebriCahyaa/Chrona`).
Your goal is to make Chrona more stable, maintainable, performant, secure, accessible, and polished while preserving existing functionality unless a task explicitly requires behavioral changes.

## 2. Core Architecture & Tech Stack

Preserve the existing Chrona architecture and toolchain boundaries:

### Toolchain Specifications
- **Gradle:** 9.7.1
- **AGP:** 9.4.1
- **Kotlin:** 2.4.10
- **Compose BOM:** 2026.09.00
- **JDK Runtime:** JDK 25
- **JDK Compilation Target:** JDK 17 (`openjdk-17-jdk-headless`)
- **compileSdk:** 37
- **compileSdkMinor:** 1
- **targetSdk:** 37
- **minSdk:** 26
- **Build Tools:** 37.0.0
- **NDK:** 28.2.13676358
- **CMake:** 3.31.5
- **C++ Standard:** C++20

### Architecture Standards
- Kotlin + Jetpack Compose UI layer
- Manual Dependency Injection managed via `AppContainer` (avoid adding DI frameworks like Hilt or Koin)
- Android component export boundaries: strictly `exported="false"` for internal activities, services, receivers, and widget providers (`MainActivity` alone is `exported="true"`)
- Time engine pulse emission: sub-second accuracy (e.g. 250ms pulse rate) without skipping seconds
- Native C++ timing layer under `app/src/main/cpp/` (`chrona_time.cpp`, `chrona_clock.cpp`)

## 3. Agent Responsibilities

### Sentinel (Security & Supply Chain)
- Security controls, manifest export attributes, permissions, secrets, secure storage, CodeQL, release signing boundaries.
- Release signing in `app/build.gradle.kts` depends on environment variables (`CHRONA_KEYSTORE_PATH`, `CHRONA_KEYSTORE_PASSWORD`, `CHRONA_KEY_ALIAS`, `CHRONA_KEY_PASSWORD`). Never commit key materials.
- Transitive dependency vulnerability resolutions patched in root `build.gradle.kts` (`resolutionStrategy`).
- Never weaken security controls to pass CI.

### Palette (UI / UX / Accessibility)
- Compose UI, Jetpack Compose screens, typography, colors, responsive layouts, accessibility.
- Preserve state hoisting, avoid unnecessary recompositions on clock surfaces, maintain stable `LazyColumn`/`LazyRow` keys.
- Preserve light/dark theme behavior and Material guidelines.

### Bolt (Performance & Runtime)
- Startup performance, memory, rendering, animation performance, CPU usage, background efficiency.
- Maintain time engine sub-second precision (never sacrifice tick precision for tick reduction).
- Benchmark/measure performance before claiming improvements.

### Generalist (Build & System Integration)
- Build scripts, compiler/Gradle errors, CI workflow maintenance, navigation contracts, timezone/DST correctness.
- Cross-domain integration while respecting Sentinel, Palette, and Bolt domain rules.

## 4. Implementation & Timezone Invariants

- Chrona is fundamentally a time application; treat timezone correctness as a critical invariant.
- Preserve Daylight Saving Time (DST) rules, historical and current timezone catalog integrity.
- Never hardcode timezones or trade correctness for performance or visual simplicity.
- Native code under `app/src/main/cpp/` must maintain C++20 compatibility, clean JNI signatures, and avoid unnecessary JNI crossings.

## 5. Validation Protocols

Execute narrowest relevant validation first:

1. **Font Assets:** Generate font assets before running lint or Android builds:
   ```bash
   python3 scripts/assets/fetch_google_assets.py
   ```
2. **Python CI & Asset Tools:**
   ```bash
   python3 -m unittest discover -s scripts/ci -p "test_*.py"
   python3 -m unittest discover -s scripts/assets -p "test_*.py"
   ```
3. **Android / Kotlin Unit Tests & Lint:**
   ```bash
   ./gradlew testDebugUnitTest
   ./gradlew lintDebug
   ./gradlew compileDebugAndroidTestKotlin
   ```
4. **Debug APK Build:**
   ```bash
   ./gradlew assembleDebug
   ```

If a check cannot be executed, explicitly state `"NOT RUN — <reason>"`. Never represent unexecuted checks as passed.

## 6. Change Accounting & Final Reporting Format

Every completed task report MUST strictly include the following structured layout:

- **Scope:** Summary of requested work.
- **Findings:** Key repository details discovered during inspection.
- **Changed:** Exact files and behavior modified.
- **Added:** New files, tests, documentation, or infrastructure.
- **Removed:** Files or code actually removed.
- **Not Removed:** Existing functionality intentionally preserved.
- **Validation:** Commands executed and actual output/results.
- **Remaining Risks:** Unverified aspects or environment limitations.
- **Commit Message:** Single clean conventional commit message.
