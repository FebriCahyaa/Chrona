<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# CHRONA — JULES AGENT OPERATING MANUAL

This file is the authoritative repository-level instruction set for Google Jules.
Read it before making changes. Prefer the latest `main` branch as the source of
truth for every new task.

## 1. Project Identity

- Project: Chrona
- Platform: Android
- Primary language: Kotlin
- UI: Jetpack Compose
- Build system: Gradle Kotlin DSL
- Application ID: `com.febricahyaa.clockapp`
- Repository default branch: `main`
- Current module graph: root project + `:app`
- Native layer: C++20 under `app/src/main/cpp/` with Java JNI boundary in `nativelayer/`

Chrona is a time and world-clock application focused on accurate timekeeping,
timezone correctness, world-clock visualization, reliable navigation, and a
refined Android user experience.

## 2. Operating Principles

1. Inspect the current repository before changing anything.
2. Treat the latest `main` as authoritative; do not use an old Jules branch
   as a baseline.
3. Preserve existing architecture and public behavior unless the task explicitly
   changes them.
4. Make the smallest coherent change that solves the task.
5. Avoid unrelated refactors, dependency churn, and speculative cleanup.
6. Never invent files, APIs, tests, benchmark results, screenshots, or
   verification results.
7. Never claim a build, test, lint, visual check, or performance improvement
   passed unless it was actually executed and observed.
8. Never disable CI, lint, tests, security checks, or release gates to hide a
   failure.
9. Never commit secrets, signing material, API keys, tokens, or private data.
10. Never force-push or rewrite shared history.
11. Do not push directly to `main` unless the task explicitly authorizes it.
12. Preserve existing copyright headers and repository conventions.

## 3. Jules Agent Roles

### Sentinel — Security

Own:
- AndroidManifest and permission review
- secure storage and secret handling
- dependency and supply-chain security
- network security
- CI/CD permissions
- CodeQL and security workflow correctness
- license and policy audit integration
- release-signing boundaries

Do not:
- redesign UI without a security reason
- alter timing or performance behavior unrelated to security
- weaken security controls for convenience

### Palette — UI / UX

Own:
- Jetpack Compose screens and components
- layout, typography, spacing, theming, color and motion
- accessibility
- navigation presentation and interaction design
- World Clock visual and interaction UX
- Liquid Glass-inspired visual language where already applicable

Do not:
- change signing or security controls
- change native timing implementation without necessity
- perform broad business-logic refactors unrelated to the UI task

### Bolt — Performance

Own:
- startup and rendering performance
- Compose recomposition and allocation efficiency
- memory and battery efficiency
- animation efficiency
- background work efficiency
- build-time performance when directly relevant

Rules:
- correctness comes before benchmarks
- do not add arbitrary Android system properties
- do not bypass thermal, security, or lifecycle safeguards
- support performance claims with measurable evidence

### Generalist — Cross-cutting Engineering

Use for:
- compiler/build failures
- navigation or state-management integration
- data/timezone correctness
- test infrastructure
- CI/tooling issues
- changes spanning multiple roles

When a task crosses role boundaries, preserve each role's constraints and make
the change narrowly scoped.

## 4. Current Build Contract

Use the repository's current pins as the source of truth.

- Gradle Wrapper: 9.7.1
- Android Gradle Plugin: 9.4.1
- Kotlin: 2.4.10
- Compose BOM: 2026.09.00
- JDK 25: Gradle runtime
- JDK 17: Kotlin/Java compilation toolchain
- compileSdk: 37
- compileSdkMinor: 1
- targetSdk: 37
- minSdk: 26
- Android Build Tools: 37.0.0
- NDK: 28.2.13676358
- CMake: 3.31.5
- Native language level: C++20
- Native optimization flags currently include `-O2`, `-ffast-math`,
  and `-fvisibility=hidden`
- Core library desugaring is enabled

Do not downgrade the toolchain merely to make a local environment easier.
Before changing a pinned version, inspect the version catalog, setup action,
CI workflows, compatibility constraints, and related documentation.

## 5. Canonical Toolchain Setup

The canonical CI toolchain definition is:

`.github/actions/setup-android/action.yml`

It installs:
- JDK 25 runtime
- JDK 17 compilation toolchain
- Gradle through the Gradle setup action
- Android command-line tools
- platform-tools
- Android platform 37.1
- Build Tools 37.0.0
- CMake 3.31.5
- NDK 28.2.13676358 when native setup is enabled

Do not create a second incompatible toolchain definition.

## 6. Repository Architecture

Current source areas include:

`app/src/main/java/com/febricahyaa/clockapp/`
- `alarm/`
- `core/`
- `data/location/`
- `data/timezone/`
- `di/`
- `model/`
- `navigation/`
- `nativelayer/` — Java JNI bridge, native Kotlin facades and low-latency alert audio entry point
- `notification/`
- `time/`
- `timer/`
- `ui/`
- `widget/`

Native timing:
- `app/src/main/cpp/chrona_time.cpp`
- `app/src/main/cpp/chrona_clock.cpp`
- `app/src/main/cpp/CMakeLists.txt`

Tests:
- `app/src/test/`
- `app/src/androidTest/`

Supporting infrastructure:
- `config/`
- `docs/`
- `scripts/`
- `.github/actions/`
- `.github/workflows/`
- `third_party/licenses/`

Do not infer a different module layout without inspecting the current tree.

## 7. Core Functional Invariants

Preserve these unless the task explicitly changes them:

- accurate time display and clock updates
- correct timezone conversion
- deterministic and validated timezone catalog behavior
- reliable navigation
- persistent user settings/state where currently implemented
- correct loading, empty, and error states
- accessibility semantics
- light and dark theme support
- responsive world-clock interaction
- Material 3-only application UI (icon vector imports are allowed)
- foreground-only current-location UX
- fixed/offline world map visualization
- persisted seconds-display mode
- native timing integration and its public contracts
- localization and source/translation audit behavior

For timezone work:
- use the repository's timezone catalog and validation tooling
- do not hard-code timezones from memory when an existing source or catalog
  already exists
- treat DST transitions and historical/current rules as correctness concerns

## 8. Android / Compose Rules

- Follow existing state-hoisting and ViewModel patterns.
- Avoid introducing global mutable state.
- Keep composables focused on presentation and event dispatch.
- Preserve stable keys in lazy lists when item identity matters.
- Keep expensive calculations out of frequently recomposed UI.
- Use lifecycle-aware collection for UI state where the existing architecture
  expects it.
- Preserve accessibility labels, roles, and touch targets.
- Do not add visual dependencies when an existing Compose/Material primitive
  is sufficient.
- Avoid unnecessary animation on frequently updating clock surfaces.

## 9. Navigation and State

Before changing navigation:
1. inspect the navigation graph and route definitions
2. find all callers and tests
3. preserve route argument contracts
4. check persistence and back-stack behavior
5. add or update tests for regressions when appropriate

Do not silently rename or remove routes used by existing screens.

## 10. Native C++ Timing Rules

The native timing layer is performance-sensitive and correctness-sensitive.

- Preserve ABI/API contracts unless the task explicitly changes them.
- Keep C++20 compatibility.
- Avoid undefined behavior and unchecked pointer arithmetic.
- Avoid unnecessary JNI crossings in hot paths.
- Do not claim timing improvements without measurements.
- Any native change should include the narrowest relevant build/test validation.

## 11. CI/CD Contract

Chrona intentionally exposes exactly five workflow files. Do not create separate Telegram, security, localization, maintenance, or toolchain workflow files. Put related jobs into the appropriate primary workflow.

### `update-commit.yml`

Main-branch and scheduled health checks: source/header audit, Material 3 audit, workflow contract audit, dependency graph, CodeQL, Scorecard, Telegram renderer tests, and commit summary.

### `debug-build.yml`

Debug validation plus a four-ABI native build matrix (`arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64`). APK upload is manual-only.

### `release-build.yml`

Manual signed APK/AAB build, provenance attestation and optional GitHub Release publication. Signing secrets remain inside the `release` Environment.

### `sync-source.yml`

Source/resource synchronization, localization synchronization and dependency graph checks.

### `pull-request-issue.yml`

Pull-request source/build/security checks and issue intake validation. Dependabot is handled as a PR event with a distinct Telegram renderer.

Push and pull-request runs must not publish APK/ZIP artifacts.

## 12. Required Validation

Choose validation based on the changed surface.

For Kotlin/Compose changes, prefer as applicable:
```bash
./gradlew testOssDebugUnitTest
./gradlew lintOssDebug
./gradlew assembleOssDebug
```

For repository/tooling changes, also consider:
```bash
bash scripts/audit/source-audit.sh
```

For native changes, include a build path that compiles the native layer.

For release changes, validate the signing verification path without exposing
credentials.

For localization/timezone changes, run the relevant scripts under
`scripts/localization/` or `scripts/audit/`.

Before declaring success, check:
- compilation
- tests
- lint
- affected scripts/YAML syntax
- generated reports when relevant

If the environment prevents a check, report it as not run or blocked.

## 13. Change Scope Discipline

Before modifying a shared component:
1. find all usages
2. inspect its public API
3. inspect existing tests
4. identify persistence/navigation/side effects
5. make the smallest safe change

Avoid:
- opportunistic formatting across unrelated files
- dependency upgrades unrelated to the task
- broad renames
- deleting "unused" code without evidence
- changing CI naming that may affect branch protection or integrations

## 14. Git and Branch Discipline

- Base work on the latest `main`.
- Use a dedicated task branch when branch creation is available.
- Keep one coherent task per branch/PR.
- Do not force-push.
- Do not rewrite shared history.
- Use descriptive conventional commit messages.
- Do not include generated binaries or build output unless explicitly required.

Suggested commit style:
- `fix: ...`
- `feat: ...`
- `perf: ...`
- `refactor: ...`
- `build: ...`
- `ci: ...`
- `docs: ...`
- `test: ...`
- `security: ...`

## 15. Current Repository Invariants

Do not reintroduce previously removed infrastructure merely because it appears
in older commits or stale branches.

In particular:
- the old Device QA emulator workflow is not part of the current core CI
- historical Device QA documentation may remain under archive/history
- `app/src/androidTest/` remains valid for local/device testing
- `adb` and platform-tools remain valid because maintenance/tooling uses them
- localization, maintenance, Telegram, Dependabot, CodeQL, Scorecard, release
  signing, provenance, and source/license/timezone audits remain active concerns

Use current `main` rather than historical commits as the behavioral baseline.

## 16. Documentation Rules

When a change affects architecture, CI, build requirements, release behavior,
localization, timezone behavior, or security boundaries:
- update the closest relevant documentation
- keep documentation synchronized with actual files
- do not document unverified claims

Prefer updating existing docs over creating duplicate documentation.

## 17. Jules Task Execution Protocol

For each task, Jules should follow this order:

### Phase A — Inspect
- read `AGENTS.md`
- inspect current branch/ref
- inspect relevant files and tests
- identify constraints and affected surfaces

### Phase B — Plan
State:
- objective
- files likely to change
- risks
- validation commands

Do not start broad unrelated work.

### Phase C — Implement
- make the smallest coherent patch
- preserve APIs and conventions
- add/update tests when the behavior changes
- update docs when architecture or operational behavior changes

### Phase D — Validate
Run the narrowest relevant checks, then broader checks when practical.
Capture exact failures and blockers.

### Phase E — Report
Every completed task must report:

**Scope**
- what the task covered

**Findings**
- important repository facts discovered

**Changed**
- files and behavior modified

**Added**
- new files/tests/docs/assets

**Removed**
- deleted files/behavior

**Not Removed**
- related functionality intentionally preserved

**Validation**
- exact commands run and their result

**Remaining Risks**
- any unverified areas, environment blockers, or follow-up concerns

Do not use "passed", "verified", "fixed", or "optimized" for work that was not
actually validated.

## 18. Priority Order

When instructions conflict, use this order:

1. explicit task requirements
2. repository security and correctness constraints
3. this AGENTS.md
4. existing architecture and project conventions
5. convenience or stylistic preference

Never choose convenience over correctness or security.

## 19. Final Checklist

Before finishing a task:

- [ ] latest `main` inspected
- [ ] scope kept focused
- [ ] no secrets added
- [ ] no security/test/lint gate bypassed
- [ ] relevant tests/lint/build executed
- [ ] documentation updated when required
- [ ] Changed / Added / Removed / Not Removed reported
- [ ] validation results reported honestly
- [ ] remaining risks called out
