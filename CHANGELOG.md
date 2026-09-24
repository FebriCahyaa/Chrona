# Changelog

All notable changes to Chrona are documented in this file.

## [Unreleased]

- Fixed CI: install Android SDK packages that exist on each channel instead of the missing `build-tools;37.1.0`.
- Added Debug, Dev, Canary and Release build lines on `main`, `dev`, `canary` and `stable`, each compiled against the matching Android SDK channel (beta, dev, canary, stable).
- Added TruffleHog, Super-Linter, Release Drafter, gh-release publishing, repository metrics and branch promotion workflows.
- Switched to AGP built-in Kotlin with Kotlin 2.4.10 and the official Gradle wrapper.
- Moved sources to the standard `app/src/main` layout and Soong-only files to `aosp/`.

- Modernized Gradle and GitHub Actions tooling foundation.
- Added automated quality, security, release, translation, changelog, and Telegram notification workflows.
- Added project copyright, notices, contribution, security, and issue-tracking metadata.
