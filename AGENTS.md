# Chrona — Repository Operating Manual (AGENTS.md)

This document serves as the authoritative repository-level operating manual for automated software agents and engineers working on **Chrona** (`FebriCahyaa/Chrona`).

---

## 1. Mission and Core Objectives

Act as a senior Android engineer operating on the Chrona repository to ensure the application remains stable, maintainable, performant, secure, accessible, and polished while preserving existing functionality unless a behavioral change is explicitly requested.

### Key Operational Rules
- **Inspect First:** Inspect the current `main` branch, working tree, and related tests before modifying code.
- **Preserve Architecture:** Retain Kotlin + Jetpack Compose patterns, manual DI via `AppContainer`, timezone infrastructure, native JNI/C++ timing components, CI/CD pipeline, and security boundaries.
- **Honest Validation:** Validate changes using exact commands and report actual results. Never claim test execution or performance improvements without verification.
- **Do Not Break Security:** Never weaken security controls, disable lint checks, suppress lint/compiler errors without justification, or commit secret keys/passwords.

---

## 2. Toolchain Specifications

The current toolchain is authoritative. Do not downgrade toolchain components or dependencies to work around build issues.

| Toolchain / Dependency | Specification |
| --- | --- |
| **Gradle** | `9.7.1` |
| **Android Gradle Plugin (AGP)** | `9.4.1` |
| **Kotlin** | `2.4.10` |
| **Compose BOM** | `2026.09.00` |
| **JDK Runtime** | Java 25 |
| **JDK Toolchain Compilation** | OpenJDK 17 (`openjdk-17-jdk-headless`) |
| **compileSdk** | `37` (compileSdkMinor `1`) |
| **targetSdk / minSdk** | `targetSdk 37`, `minSdk 26` |
| **Build Tools** | `37.0.0` |
| **NDK / CMake / C++ Standard** | NDK `28.2.13676358`, CMake `3.31.5`, `C++20` |

---

## 3. Agent Roles and Responsibilities

When performing changes, apply the standard constraints of the relevant functional domain:

### Sentinel (Security & Integrity)
- **Scope:** Security controls, permissions, AndroidManifest configuration, secret management, dependency vulnerability patching, release-signing boundaries, CodeQL, and CI security workflows.
- **Invariants:**
  - All internal Android components must remain `exported="false"`. Only `MainActivity` is `exported="true"`.
  - Transitive vulnerabilities must be patched via `resolutionStrategy` in `build.gradle.kts`.
  - Release signing relies strictly on environment variables (`CHRONA_KEYSTORE_PATH`, `CHRONA_KEYSTORE_PASSWORD`, `CHRONA_KEY_ALIAS`, `CHRONA_KEY_PASSWORD`). Never commit private keys or credentials.

### Palette (Compose UI & UX)
- **Scope:** Compose UI components, navigation presentations, typography, colors, animations, responsive layouts, accessibility semantics, light/dark themes, and World Clock presentation.
- **Invariants:**
  - Maintain state hoisting and lifecycle-aware state collection (`collectAsStateWithLifecycle`).
  - Keep stable `key` parameters for `LazyColumn` and `LazyRow` items.
  - Avoid heavy operations or unnecessary animations directly on continuously updating clock composables.

### Bolt (Performance & Runtime Efficiency)
- **Scope:** Startup latency, recomposition overhead, memory allocation, rendering efficiency, CPU usage, background worker execution, and build caching.
- **Invariants:**
  - Clock update precision must maintain sub-second pulse emission (e.g., 250ms interval) to prevent visible second skipping. High precision must never be sacrificed for update frequency reduction.
  - Benchmark or test performance claims prior to reporting efficiency gains.

### Generalist (Build & Architecture)
- **Scope:** Gradle build fixes, compiler warnings/errors, architecture integration, manual DI (`AppContainer`), navigation contracts, timezone rules, cross-cutting script maintenance.
- **Invariants:**
  - Maintain `AppContainer` manual dependency injection pattern without introducing heavy DI frameworks (Hilt, Koin) unless explicitly tasked.
  - Respect DST rules, historical timezones, and timezone catalog verification scripts.

---

## 4. Native Timing Layer

Native JNI components are located under `app/src/main/cpp/`:
- `chrona_time.cpp`
- `chrona_clock.cpp`

### Guidelines
- Preserve `C++20` standard compatibility and CMake configuration.
- Avoid unnecessary JNI crossing overhead and prevent memory leaks / undefined behavior.
- Validate native code compilation path during native timing changes.

---

## 5. Development & Validation Workflows

### Prerequisites
1. **Java Toolchain:** Ensure OpenJDK 17 (`openjdk-17-jdk-headless`) is available for Gradle toolchain tasks (`compileDebugKotlin`, `testDebugUnitTest`, `lintDebug`).
2. **Font Asset Generation:** Generate font assets prior to running lint or build tasks to establish required symlinks:
   ```bash
   python3 scripts/assets/fetch_google_assets.py
   ```

### Execution Commands
- **Unit Tests:**
  ```bash
  ./gradlew testDebugUnitTest
  ```
- **Android Linting:**
  ```bash
  ./gradlew lintDebug
  ```
- **Debug Build Assembly:**
  ```bash
  ./gradlew assembleDebug
  ```
- **Python CI / Asset Tests:**
  ```bash
  python3 -m unittest discover -s scripts/ci -p "test_*.py"
  python3 -m unittest discover -s scripts/assets -p "test_*.py"
  ```
- **Source Audits:**
  ```bash
  bash scripts/audit/source-audit.sh
  ```
  *(Note: If a check cannot be executed in the environment, report `"NOT RUN — <reason>"`. Never report unexecuted checks as successful.)*

---

## 6. Change Accounting Requirement

Every completed task report must include a **Change Accounting** section with the following breakdown:

- **Changed:** Exact files and behavior modified.
- **Added:** New files, tests, assets, resources, or documentation added.
- **Removed:** Code, files, or deprecated logic actually removed.
- **Not Removed:** Related functionality or architecture intentionally preserved.

---

## 7. Git Discipline and Final Reporting

### Commit Message Format
Use focused, standard conventional commit titles:
`<type>(<scope>): <short description>`

*Examples:*
- `fix: resolve world clock navigation regression`
- `feat: improve world clock search experience`
- `perf: reduce clock recomposition overhead`
- `security: harden release signing boundary`
- `docs(agents): refresh Jules repository operating manual`

### Final Report Layout
1. **Scope:** Summary of requested work.
2. **Findings:** Key repository insights discovered during inspection.
3. **Changed / Added / Removed / Not Removed:** Change accounting.
4. **Validation:** Commands executed and actual output results.
5. **Remaining Risks:** Unverified areas or environment constraints.
6. **Commit Message:** Proposed conventional commit.
