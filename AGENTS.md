# Chrona AI Agent Operating Manual (AGENTS.md)

This document is the repository-level operating manual for AI agents (including Jules) working on the Chrona Android repository (`FebriCahyaa/Chrona`).

---

## 1. Mission Standard

Act as a senior Android engineer working directly on the current repository state.
Your goal is to make Chrona more stable, maintainable, performant, secure, accessible, and polished while preserving existing functionality unless the task explicitly requires a behavioral change.

Always inspect the codebase before modifying anything:
1. Inspect the latest `main` branch (the source of truth).
2. Inspect the working tree and relevant files.
3. Identify existing architecture and implementation.
4. Find usages of any shared components intended for modification.
5. Inspect related tests before changing behavior.
6. Identify the smallest set of files required for the task.

---

## 2. Architecture & Toolchain

### Architecture Constraints
- **UI & Framework**: Kotlin + Jetpack Compose & Material Design. Preserve state hoisting, stable LazyColumn/LazyRow keys, light/dark theme behavior, and accessibility semantics.
- **State & DI**: Preserve existing ViewModel, `AppContainer` manual DI, and state patterns.
- **Navigation & Clock Systems**: Preserve navigation contracts, timezone infrastructure, persistence behavior, localization infrastructure, and native C++ timing layer.
- **Security & Privacy**: Internal components must remain non-exported (`exported="false"`) unless explicitly required. Never weaken security controls or commit secrets.

### Authoritative Toolchain Versions
- **Gradle**: 9.7.1
- **AGP**: 9.4.1
- **Kotlin**: 2.4.10 / 2.4.20
- **Compose BOM**: 2026.09.00
- **JDK Runtime**: JDK 25 runtime / JDK 17 compilation (`jvmToolchain(17)`)
- **SDK Target**: `compileSdk` 37 (with `compileSdkMinor` 1/2 support), `targetSdk` 37, `minSdk` 32 (or 26 fallback)
- **Native**: C++20, CMake 3.31.5, NDK 28.2.13676358

---

## 3. Agent Responsibilities

- **Sentinel**: Security, permissions, Manifest, dependency vulnerabilities, secrets, secure storage, network security, CodeQL, CI permissions, release signing.
- **Palette**: Compose UI, UX, navigation presentation, typography, spacing, colors, animations, accessibility, responsive layouts, World Clock experience.
- **Bolt**: Startup performance, Compose recomposition, memory, rendering, animation performance, CPU usage, battery efficiency, background work, build performance.
- **Generalist**: Build failures, compiler errors, Gradle issues, CI failures, architecture integration, state management, navigation correctness, timezone correctness.

---

## 4. Engineering Invariants

### Time & Timezone Correctness
- Clock update precision must never be sacrificed for reduced update frequency.
- Sub-second pulse accuracy (e.g., 250ms time engine pulse rate) must be preserved to avoid visible second skipping.
- Respect DST transitions and historical timezone rules without hard-coded assumptions.

### Native Timing Layer
- C++ timing source located under `app/src/main/cpp/` (`chrona_time.cpp`, `chrona_clock.cpp`).
- Maintain C++20 compatibility, JNI contracts, and zero unnecessary JNI crossings.

---

## 5. Validation Standards

Before submitting changes, execute appropriate validation steps:

```bash
# Generate font assets required for lint/build
python3 scripts/assets/fetch_google_assets.py

# Execute unit tests, lint, and assemble debug APK
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

If any check cannot be executed in the environment, report explicitly:
`NOT RUN — <reason>`

---

## 6. Change Accounting & Final Report Format

Every completed task report MUST include:

### Change Accounting
- **Changed**: Files and behavior modified.
- **Added**: New files, tests, resources, or docs introduced.
- **Removed**: Files, code, behavior, or infrastructure removed.
- **Not Removed**: Important functionality intentionally preserved.

### Final Report Sections
1. **Scope**: What was requested.
2. **Findings**: Important repository facts discovered.
3. **Changed**: Exact files and behavior changed.
4. **Added**: Everything newly introduced.
5. **Removed**: Everything actually removed.
6. **Not Removed**: Important functionality deliberately preserved.
7. **Validation**: Exact commands executed and their actual results.
8. **Remaining Risks**: Anything not fully verified or blocked.
9. **Commit Message**: One clean conventional commit message.
