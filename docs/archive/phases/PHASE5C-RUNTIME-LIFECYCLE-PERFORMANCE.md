# Phase 5C — Runtime Lifecycle & Performance Hardening

## Scope

Phase 5C reduces foreground rendering work and unnecessary Compose recomposition without changing timer/stopwatch semantics.

## Changes

- Bind ChronaTimeEngine high-frequency ticker eligibility to the visible Activity lifecycle.
- Keep monotonic timestamps authoritative while foreground ticker work is suspended in background.
- Reconcile engine state immediately when the app returns to foreground.
- Scope timer, stopwatch, alarm, world-clock, and updater UI collectors to their active navigation destination instead of collecting every high-frequency state at the root ClockApp composition.
- Keep settings/onboarding collectors at the root because they affect global theme and startup flow.
- Add pure ticker eligibility policy coverage.

## Guarantees

- Backgrounding the app does not pause a running stopwatch or timer.
- AlarmManager remains responsible for durable timer expiry.
- No fixed one-second counter loop is introduced.
- Navigation routes, repositories, schedulers, and data persistence remain unchanged.
