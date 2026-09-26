# Chrona — Agent Operating Manual

This repository operating manual defines guidelines and constraints for autonomous software engineering agents working on Chrona ("FebriCahyaa/Chrona").

---

## 1. Core Operating Principles

- **Inspect Before Changing**: Always read relevant documentation, code, and test cases before modifying anything. The `main` branch is the source of truth.
- **Preserve Architecture**: Do not perform broad rewrites unless explicitly requested. Preserve Kotlin + Jetpack Compose architecture, ViewModel/state patterns, native C++ timing layer (`app/src/main/cpp/`), navigation contracts, timezone infrastructure, security controls, and release-signing boundaries.
- **Verification**: Verify every file change using read-only tools or bash verification before marking plan steps complete.

---

## 2. Toolchain Baseline

Treat the repository's current toolchain as authoritative:
- Gradle 9.7.1
- AGP 9.4.1
- Kotlin 2.4.10
- Compose BOM 2026.09.00
- JDK 21/25 runtime, JDK 17 target compilation
- compileSdk 37 (or 37.2 per channel)
- minSdk 26 / targetSdk 37
- NDK 28.2.13676358 & CMake 3.31.5 (C++20)

Do not downgrade dependencies or toolchain versions simply to bypass a build or test failure.

---

## 3. Specialized Roles & Responsibilities

- **Sentinel**: Security, permissions, `AndroidManifest.xml`, dependency review, secure storage, CodeQL, CI permissions, release signing, supply-chain security. Never weaken security controls to pass CI.
- **Palette**: Compose UI, UX, navigation presentation, typography, spacing, colors, animations, accessibility semantics, responsive layouts, World Clock experience.
- **Bolt**: Startup performance, Compose recomposition reduction, memory, rendering, battery efficiency, background work.
- **Generalist**: Build failures, compiler errors, Gradle configuration, state management, timezone correctness, cross-cutting integrations.

---

## 4. Subsystem Invariants

### Jetpack Compose
- Preserve state hoisting and avoid heavy operations directly inside composables.
- Maintain stable `LazyColumn`/`LazyRow` keys.
- Preserve accessibility semantics and light/dark theme behavior.
- Collect state in a lifecycle-aware manner.

### Timezone & Native Timing
- Timezone correctness is critical; preserve DST rules and avoid hard-coded timezone assumptions.
- Native code under `app/src/main/cpp/` (`chrona_time.cpp`, `chrona_clock.cpp`) must maintain C++20 compatibility and JNI contracts without unnecessary JNI crossings.
- Sub-second pulse precision (e.g., 250ms rate) must be preserved to avoid visible second skipping.

---

## 5. Validation Rules

Run narrow, relevant validation tasks locally for changes:
- Kotlin/Compose changes: `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, `./gradlew assembleDebug`
- Python assets/scripts: execute test scripts (e.g. `python3 scripts/assets/test_fetch_google_assets.py`)
- If a check cannot be executed in the environment, report explicitly: `"NOT RUN — <reason>"`. Never claim unexecuted checks passed.

---

## 6. Change Accounting & Reporting Format

Every final task summary MUST follow this structure:

```markdown
### Scope
[Summary of requested task]

### Findings
[Facts discovered during inspection]

### Change Accounting
#### Changed
[Files and behavior modified]

#### Added
[Files, tests, resources, docs introduced]

#### Removed
[Files or code removed]

#### Not Removed
[Functionality intentionally preserved]

### Validation
[Exact commands executed and results]

### Remaining Risks
[Unverified areas or environment limitations]

### Commit Message
[Clean conventional commit message]
```
