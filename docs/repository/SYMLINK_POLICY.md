<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Symlink Policy

Chrona should **not** use symbolic links for application source, Gradle build logic, Android resources, or `.github` workflows.

Symlinks make ZIP exports, Windows checkouts, Android Studio indexing, and some CI runners less predictable. For shared functionality use:

- Gradle version catalogs and convention plugins for build logic.
- Composite/reusable GitHub Actions for CI behavior.
- Versioned scripts under `scripts/` for repository operations.
- A dedicated shared tooling repository when logic must be reused across multiple projects.

A symlink may be used for a purely local developer convenience, but it must not be required to build or test Chrona.


## Preferred alternatives

Use Gradle version catalogs and convention plugins for shared Gradle logic, composite/reusable GitHub Actions for CI logic, and parameterized scripts for local/CI commands. These mechanisms work consistently on GitHub-hosted Linux runners and local Android/Termux environments without relying on filesystem symlink preservation.
