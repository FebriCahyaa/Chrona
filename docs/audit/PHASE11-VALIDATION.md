<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Phase 11 Repository Hardening Validation

## Scope

This phase cleans the active source tree, reorganizes scripts and GitHub Actions, adds dedicated Telegram notification channels, strengthens license/copyright audits, documents localization/world-clock provenance, and removes source files proven unused by repository-wide active-reference analysis.

## Passed checks

- Bash syntax validation for active scripts.
- C++20 JNI source syntax check with clang++.
- Project copyright coverage for active source/configuration files; historical `docs/archive/` records are excluded.
- Android resource reference audit.
- Dependency license register audit: 33 declared/pinned artifacts documented.
- Localization resource audit: no stale keys across translated resource sets.
- Dynamic timezone catalog audit.
- Telegram notification renderer unit tests.
- GitHub Actions YAML parsing.
- No internal ChatGPT citation markers in repository documentation.
- No Python bytecode artifacts in the final source tree.

## Dependency and licensing boundary

The machine-readable license register covers artifacts declared in the Gradle version catalog plus intentionally pinned transitive constraints. Resolved dependency graphs can change, so pull-request Dependency Review remains a second validation layer. Upstream license sources are indexed under `third_party/licenses/`.

## World Clock provenance

Chrona does not vendor a static timezone database. The production catalog is generated from Android ICU/IANA runtime timezone identifiers. The current IANA reference used in this audit is release 2026d, published 2026-09-11: https://www.iana.org/time-zones/releases/2026d.

## Gradle limitation in this environment

`./gradlew testDebugUnitTest` could not be completed in the offline analysis environment because Gradle 9.7.1 was not cached and `services.gradle.org` was unreachable. The repository is therefore not described as fully Gradle-green until a networked GitHub Actions runner executes the complete test/build matrix.
