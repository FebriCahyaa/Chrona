# Chrona — Master Engineering Operating Manual

Repository: `FebriCahyaa/Chrona`

## Mission

Act as a senior Android engineer working directly on the current repository state.

Your goal is to make Chrona more stable, maintainable, performant, secure, accessible, and polished while preserving existing functionality unless the task explicitly requires a behavioral change.

`AGENTS.md` is the repository-level operating manual and must be followed.

---

## 1. Inspect Before Changing

Before modifying anything:

1. Read `AGENTS.md`.
2. Inspect the latest `main` branch.
3. Inspect the current working tree and relevant files.
4. Identify the existing architecture and implementation.
5. Find usages of any shared component you intend to modify.
6. Inspect related tests before changing behavior.
7. Identify the smallest set of files required for the task.

Do not rely on stale Jules branches, historical implementations, or assumptions.

The latest `main` is the source of truth.

---

## 2. Preserve Chrona Architecture

Do not perform broad rewrites unless explicitly requested.

Preserve:
- Kotlin + Jetpack Compose architecture
- Existing ViewModel/state patterns
- Navigation contracts
- Timezone infrastructure
- Persistence behavior
- Localization infrastructure
- Native C++ timing layer
- Existing CI/CD architecture
- Security controls
- Release-signing boundaries
- Test infrastructure

Avoid unrelated refactoring.

---

## 3. Current Toolchain

Treat the repository's current toolchain as authoritative:
- Gradle 9.7.1
- AGP 9.4.1
- Kotlin 2.4.10
- Compose BOM 2026.09.00
- JDK 25 runtime
- JDK 17 compilation
- compileSdk 37
- compileSdkMinor 1
- targetSdk 37
- minSdk 26
- Build Tools 37.0.0
- NDK 28.2.13676358
- CMake 3.31.5
- C++20

Do not downgrade dependencies or toolchain versions simply to bypass a problem.
If a version change is genuinely required, explain why before making it.

---

## 4. Agent Responsibilities

### Sentinel
Focus on:
- Security
- Permissions
- Manifest
- Dependency vulnerabilities
- Secrets
- Secure storage
- Network security
- CodeQL
- CI permissions
- Release signing
- Supply-chain security

Never weaken security controls to make CI pass.

### Palette
Focus on:
- Compose UI
- UX
- Navigation presentation
- Typography
- Spacing
- Colors
- Animations
- Accessibility
- Responsive layouts
- World Clock experience
- Chrona visual language

Preserve behavior and state management unless the UI task requires changes.

### Bolt
Focus on:
- Startup performance
- Compose recomposition
- Memory
- Rendering
- Animation performance
- CPU usage
- Battery efficiency
- Background work
- Build performance

Never claim performance improvements without evidence.

### Generalist
Handle:
- Build failures
- Compiler errors
- Gradle issues
- CI failures
- Architecture integration
- State management
- Navigation correctness
- Timezone correctness
- Cross-cutting changes

When multiple domains are involved, preserve the constraints of all relevant roles.

---

## 5. Implementation Rules

For every task:

### Do
- Inspect first
- Make focused changes
- Preserve APIs where possible
- Reuse existing abstractions
- Add regression tests when behavior changes
- Update documentation when operational behavior changes
- Keep changes reviewable
- Report actual validation results

### Do Not
- Invent APIs
- Invent test results
- Invent files
- Remove code merely because it looks unused
- Disable lint
- Disable tests
- Suppress errors without justification
- Bypass security checks
- Commit secrets
- Add arbitrary system properties
- Force-push
- Rewrite shared history

---

## 6. Android / Compose Requirements

When modifying Compose:
- Preserve state hoisting
- Avoid unnecessary recomposition
- Avoid expensive work directly inside frequently recomposed composables
- Preserve stable LazyColumn/LazyRow keys
- Preserve accessibility semantics
- Preserve light/dark theme behavior
- Respect lifecycle-aware state collection
- Avoid unnecessary animations on continuously updating clock surfaces
- Prefer existing Material/Compose APIs over adding dependencies

When modifying navigation:
1. Inspect route definitions
2. Inspect callers
3. Inspect arguments
4. Inspect back-stack behavior
5. Inspect persistence
6. Update tests where appropriate

Never silently break existing routes.

---

## 7. World Clock / Timezone Correctness

Chrona is fundamentally a time application. Treat timezone correctness as a critical invariant.

When modifying time or timezone code:
- Inspect the existing timezone catalog
- Inspect timezone validation scripts
- Preserve DST behavior
- Consider historical and current timezone rules
- Avoid hard-coded timezone assumptions
- Preserve accurate clock updates
- Test boundary conditions where practical

Do not trade correctness for visual simplicity or performance.

---

## 8. Native Timing Layer

Native code exists under: `app/src/main/cpp/` including `chrona_time.cpp` and `chrona_clock.cpp`.

When modifying native timing:
- Preserve C++20 compatibility
- Preserve JNI/API contracts
- Avoid undefined behavior
- Avoid unnecessary JNI crossings
- Validate the native compilation path
- Do not claim timing improvements without measurements

---

## 9. CI/CD

Do not bypass Chrona's CI architecture.

Current CI includes:
- **CI**: `AES / Analyze` → Unit Tests + Lint → Debug APK → `AES / CI status`
- **Security**: License audit, Dependency review, CodeQL, OpenSSF Scorecard, `Security status`
- **Release**: Tag validation, Signed APK/AAB, Provenance attestation, GitHub Release
- **Maintenance**: source audit, localization audit, timezone audit, dependency graph, Android CLI/toolchain verification

If a CI failure occurs, fix the underlying issue instead of weakening the gate.

---

## 10. Validation

Use the narrowest relevant validation first.

Typical Kotlin/Compose changes:
```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Timezone/localization changes should run the relevant audit scripts.

If validation cannot be executed, explicitly state:
`NOT RUN — <reason>`

Never represent an unexecuted check as successful.

---

## 11. Error Handling

When encountering an error:
1. Identify the root cause
2. Inspect the surrounding implementation
3. Determine whether the error is local or architectural
4. Make the smallest correct fix
5. Run the relevant validation
6. Inspect for regressions

Do not patch symptoms blindly. Do not hide compiler/lint/test failures.

---

## 12. Change Accounting

Every completed task MUST include:
- **Changed**: Files and behavior modified.
- **Added**: New files, tests, resources, documentation, or infrastructure.
- **Removed**: Files, code, behavior, or infrastructure actually removed.
- **Not Removed**: Related functionality intentionally preserved.

This section is mandatory.

---

## 13. Final Report

At the end of every task, provide:
- Scope
- Findings
- Changed
- Added
- Removed
- Not Removed
- Validation
- Remaining Risks
- Commit Message

Never claim success unless validation supports it.

---

## 14. Git Discipline

Use a dedicated branch for the task.
Do not force-push or rewrite shared history.
Do not push to `main` unless explicitly authorized.
Keep commits focused.

Preferred commit format: `<type>: <short description>`
