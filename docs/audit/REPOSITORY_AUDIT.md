<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Repository Audit

## Structural cleanup

- Historical phase handoffs moved out of the root.
- Vendored metadata directories for AOSP/Material icons removed because the Android build does not consume them.
- Phase-specific automation scripts removed from the active `scripts/` tree.
- Active scripts are organized by purpose: audit, CI, development, localization, release, Telegram, and world-clock diagnostics.

## Safety rule

No source/module/resource was removed solely because its filename looked unused. Deletions are limited to artifacts proven to be obsolete or superseded by active tooling.

## Active quality controls

- copyright coverage
- resource reference audit
- dependency license register
- localization key audit
- dynamic timezone catalog audit
- unit/instrumentation tests
- Android lint
- CodeQL
- Dependency Review
- OpenSSF Scorecard


## Proven-dead source cleanup

The active source tree no longer contains the unused `ClockDisplay.kt`, `NightstandDialog.kt`, or `LegalContent.kt` classes. A superseded `scripts/ci/verify-android17.sh` verifier was also removed because `scripts/ci/verify-toolchain.sh` is the active toolchain gate. Historical references remain only in `docs/archive/` or the historical changelog.
