<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Contributing to Chrona

Chrona is maintained as a focused Android application. Changes should be small, testable, and easy to review.

## Development flow

1. Create a topic branch from `main`.
2. Keep one logical task per pull request.
3. Preserve the existing architecture unless a refactor is explicitly justified.
4. Add or update tests for behavior changes.
5. Run the relevant Gradle tests, lint, and build tasks locally when available.
6. Update documentation when a public workflow, architecture, or release process changes.

## Source layout

Application code is organized by responsibility under `app/src/main/java/com/febricahyaa/clockapp/`:

- `alarm/` — alarm receivers, scheduling, sound, notifications.
- `timer/` — timer scheduling and services.
- `data/` — repository implementations and external gateways.
- `model/` — immutable application models.
- `navigation/` — routes and navigation policy.
- `time/` — unified time engine and time formatting.
- `ui/` — Compose screens, components, theme, and ViewModels.

## Commit messages

Use conventional-style messages such as `fix(worldclock): ...`, `feat(ui): ...`, `refactor(ci): ...`, or `docs: ...`.

Every maintenance task that modifies the repository should document **Changed / Added / Removed / Not Removed / Verification / Scope** in its handoff or commit body.

## Pull requests

Do not commit generated build outputs, signing material, secrets, or temporary logs. Do not disable lint/test gates to make a workflow green.
