# Chrona — Repository Operating Manual (`AGENTS.md`)

This repository operating manual is the authoritative standard for engineering work on **Chrona** (`FebriCahyaa/Chrona`).

---

## 1. Mission & Engineering Principles

Chrona is an Android time application derived from AOSP DeskClock and maintained as an independently engineered, high-precision clock application.

### Core Directives
1. **Inspect Before Changing**: Read existing code, architectural patterns, and tests before making modifications.
2. **Preserve Architecture**: Maintain Jetpack Compose UI, ViewModel/state patterns, `AppContainer` manual DI, native C++ timing layer, and existing CI/CD infrastructure.
3. **Time Correctness**: Timezone and clock pulse emission are non-negotiable invariants. Sub-second pulse precision (e.g. 250ms pulse rate) must be preserved to prevent second-skipping.
4. **Security Standards**: Internal components remain `exported="false"`. Release signing relies strictly on environment variables without committing key material.
5. **Always Verify**: Validate every state change with tests and builds before marking steps complete.

---

## 2. Toolchain Specifications

The repository toolchain is strictly defined as follows:

- **Gradle**: 9.7.1
- **Android Gradle Plugin (AGP)**: 9.4.1 (built-in Kotlin)
- **Kotlin**: 2.4.10 (pin classpath below 2.4.20 for CodeQL compatibility)
- **Compose BOM**: 2026.09.00
- **JDK Runtime**: JDK 25
- **JDK Compilation**: JDK 17 (`jvmToolchain(17)`)
- **compileSdk**: 37 (`compileSdkMinor = 1`)
- **targetSdk**: 37
- **minSdk**: 26
- **Build Tools**: 37.0.0
- **NDK**: 28.2.13676358
- **CMake**: 3.31.5
- **C++ Standard**: C++20

---

## 3. Agent Responsibilities & Roles

When acting on Chrona, operate under the domain requirements of the relevant role:

### Sentinel
- Focus: Security, permissions, `AndroidManifest.xml`, dependency vulnerabilities, secrets management, secure storage, network security, CodeQL, release signing boundaries, and supply-chain security.
- Standard: Never weaken security controls or export unneeded components to pass CI.

### Palette
- Focus: Jetpack Compose UI, UX, navigation presentation, Material Design typography, spacing, color tokens, animations, accessibility semantics, responsive layouts, and World Clock visual language.
- Standard: Preserve state hoisting and theme behavior; avoid continuous high-frequency animations on time display surfaces.

### Bolt
- Focus: Startup performance, Compose recomposition reduction, memory allocation, rendering pipelines, CPU usage, battery efficiency, background work, and build performance.
- Standard: Support performance assertions with actual measurements and profiling metrics.

### Generalist
- Focus: Build failures, compiler errors, Gradle configuration, CI pipeline repair, state management, navigation correctness, timezone accuracy, and cross-cutting changes.
- Standard: Preserve constraints across all domains when making architectural edits.

---

## 4. Implementation Rules & Standards

### Jetpack Compose & Navigation
- Preserve state hoisting and ViewModel ownership.
- Avoid heavy operations directly inside composables.
- Maintain stable keys for `LazyColumn` and `LazyRow`.
- Preserve light/dark theme support and lifecycle-aware state collection (`collectAsStateWithLifecycle`).
- Do not modify navigation routes or deep links without updating callers and tests.

### Timezone & Native Timing Layer
- Timezone catalog and DST rules must be preserved. Avoid hardcoded timezone assumptions.
- Native timing files located under `app/src/main/cpp/` (`chrona_time.cpp`, `chrona_clock.cpp`) must build using C++20.
- Minimize JNI context switches and maintain native API contracts.

### Security & Dependency Invariants
- Transitive dependency vulnerabilities are managed via `resolutionStrategy` or version catalog alignment.
- Manual dependency injection is managed through `AppContainer`.
- All internal Activities, Receivers, Services, and Providers must set `exported="false"` (only `MainActivity` is `exported="true"`).

---

## 5. Validation Standards

Run the narrowest validation command first before escalating to full builds.

### Python & Script Audits
```bash
python3 -m unittest discover -s scripts/assets -p "test_*.py"
python3 -m unittest discover -s scripts/ci -p "test_*.py"
```

### Font Asset Generation
Before running Gradle lint, generate Google Sans Flex font assets to resolve symlinks:
```bash
python3 scripts/assets/fetch_google_assets.py
```

### Gradle Build & Tests
```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

If any check cannot be executed in the current environment, state explicitly: `NOT RUN — <reason>`. Never report unexecuted checks as successful.

---

## 6. Change Accounting & Final Reporting

Every completed task must provide a Change Accounting section and follow the structured final report layout.

### Change Accounting Format
- **Changed**: Files and behavior modified.
- **Added**: New files, tests, resources, or documentation added.
- **Removed**: Code, assets, or infrastructure removed.
- **Not Removed**: Existing features or code deliberately preserved.

### Final Report Structure
- **Scope**: Description of requested task.
- **Findings**: Relevant repository observations.
- **Changed**: Summary of modifications.
- **Added**: Summary of additions.
- **Removed**: Summary of removals.
- **Not Removed**: Summary of preserved features.
- **Validation**: Exact commands executed and actual outputs.
- **Remaining Risks**: Known limitations or unverified areas.
- **Commit Message**: Single clean conventional commit proposal.

---

## 7. Git Discipline

- Use a dedicated branch (`jules/...`).
- Keep commits focused and clear.
- Format commit messages as `<type>(<scope>): <description>` (e.g., `docs(agents): refresh Jules repository operating manual`).
- Do not force-push or rewrite shared history.
