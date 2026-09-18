# Phase 4B — Hero Clock + Floating Cards

## Goal
Implement the selected Dashboard concept B: a dominant Hero Clock with floating quick-access time-tool cards.

## Scope
- Preserve the existing Hero Clock, display-mode toggle, settings action, and live time source.
- On compact windows, float the four primary time-tool cards over the lower edge of the Hero Clock.
- Keep all four cards connected to existing shared-motion keys.
- Preserve wide-screen adaptive two-column layout.
- Do not alter repositories, ViewModels, schedulers, time engine, or navigation routes.

## Behavior
- Alarm, Timer, World Clock, and Stopwatch remain one-tap destinations.
- Floating surface uses a translucent Material 3 container with elevation and a stable rounded shape.
- The hero remains the visual anchor; quick actions overlap its lower edge rather than consuming a separate full-width block.
- Existing state values remain visible: next alarm and timer countdown.

## Non-goals
- No new persistence.
- No new navigation destination.
- No changes to Phase 3 motion infrastructure.
- No release/version bump.
