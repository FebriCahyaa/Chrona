# Contributing to Chrona

Chrona accepts focused changes with a clear problem statement, deterministic validation, and preserved third-party attribution.

Use pull requests for source and CI changes. Keep unrelated refactors separate. Validate Gradle lint and tests locally when the change touches Gradle-managed sources. Preserve `Android.bp` behavior for AOSP integration unless the change explicitly targets both build systems.

Commit messages should use a conventional prefix such as `feat`, `fix`, `build`, `ci`, `docs`, `refactor`, `test`, or `chore`.
