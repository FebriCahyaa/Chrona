<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
See the repository LICENSE for the project copyright notice.
-->

# Chrona Documentation

This directory contains the project documentation that is intentionally kept out of the repository root so the source tree stays easy to navigate.

## Documentation map

| Area | Document | Purpose |
| --- | --- | --- |
| Architecture | [ARCHITECTURE.md](architecture/ARCHITECTURE.md) | Kotlin, Java, C++ and JNI responsibilities |
| Audit | [AUDIT_REPORT.md](audit/AUDIT_REPORT.md) | Static audit findings and verification notes |
| Build | [BUILD_ENVIRONMENT.md](build/BUILD_ENVIRONMENT.md) | Reproducible Android build toolchain |
| Build | [sdkmanager-list.txt](build/sdkmanager-list.txt) | Android SDK package reference captured for CI maintenance |
| Design | [ICON_IMPLEMENTATION.md](design/ICON_IMPLEMENTATION.md) | Adaptive icon and widget implementation notes |
| Design | [UI_REDESIGN_NOTES.md](design/UI_REDESIGN_NOTES.md) | UI redesign notes and constraints |
| Release | [RELEASE_SIGNING.md](release/RELEASE_SIGNING.md) | Release keystore and GitHub Environment setup |
| Release | [RELEASE_CHANGELOG_AUTOMATION.md](release/RELEASE_CHANGELOG_AUTOMATION.md) | Automated version bump and changelog flow |
| Refactoring | [REFACTORING_NOTES_ID.md](refactoring/REFACTORING_NOTES_ID.md) | Refactoring rationale and implementation notes |

## Documentation policy

Documentation in this directory should describe the implementation that actually exists in the repository. Planned work must be labeled as planned; CI or runtime results must only be reported when they have been observed and recorded.

## Repository scripts

Repository maintenance scripts live in [`../scripts/`](../scripts/), including the source audit helper.
