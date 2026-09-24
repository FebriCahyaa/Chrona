# Contributing to Chrona

Chrona accepts focused changes with a clear problem statement, deterministic
validation, and preserved third-party attribution.

## Branches

Open pull requests against `main`, the integration branch. `dev`, `canary` and
`stable` only receive promotion pull requests (`main → dev → canary →
stable`), created with the **Branch Promotion** workflow. See
[docs/ci.md](../docs/ci.md).

## Changes

Keep unrelated refactors separate. Run `./gradlew spotlessApply lintDebug
testDebugUnitTest` before opening a pull request when the change touches
Gradle-managed sources. Preserve `Android.bp` behavior for AOSP integration
unless the change explicitly targets both build systems.

Use conventional commit and pull request titles such as `feat:`, `fix:`,
`build:`, `ci:`, `docs:`, `refactor:`, `test:` or `chore:`. Release Drafter
uses the pull request title to label changes and pick the next version; mark
breaking changes with `!`, for example `feat!: drop Android 7 support`.
