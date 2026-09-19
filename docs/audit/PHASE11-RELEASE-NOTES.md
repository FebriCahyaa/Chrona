<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Phase 11 Release Notes — Repository Engineering Hardening

## What changed

- Active source and tooling were reorganized by responsibility.
- Proven-unused `ClockDisplay.kt`, `NightstandDialog.kt`, and `LegalContent.kt` were removed after repository-wide active-reference checks.
- The superseded `verify-android17.sh` verifier and old root `scripts/source-audit.sh` were removed in favor of `verify-toolchain.sh` and `scripts/audit/source-audit.sh`.
- Historical handoff files remain under `docs/archive/phases/` and are not used by active builds.
- GitHub Actions were rebuilt around dedicated CI, device QA, security, maintenance, localization, toolchain, release, and Telegram responsibilities.
- The Android composite action centralizes JDK/Gradle/SDK setup.
- Telegram notification rendering is separated into CI, PR, Dependabot, and release concerns.
- License and copyright inventory checks were added and validated.
- Localization documentation now distinguishes language and country and defines expansion/QA policy.
- World Clock remains runtime-driven by Android ICU/IANA timezone data instead of a frozen database.

## Validation

- `bash scripts/audit/source-audit.sh` — PASS
- Bash syntax validation — PASS
- C++20/JNI syntax validation — PASS
- Copyright audit — PASS (215 active text/config files; historical archive excluded)
- Resource audit — PASS (263 default strings; 237 referenced keys)
- Dependency license audit — PASS (33 declared/pinned artifacts documented)
- Localization audit — PASS (no stale keys)
- Dynamic timezone catalog audit — PASS
- Telegram renderer tests — PASS (4 tests)
- GitHub Actions YAML parsing — PASS
- Clean source scans — PASS

## Gradle execution limitation

The analysis environment cannot reach `services.gradle.org`, so the Gradle wrapper cannot download Gradle 9.7.1. Full Android compilation, lint, unit-test, instrumentation, and release assembly therefore remain gated to the networked GitHub Actions runners.
