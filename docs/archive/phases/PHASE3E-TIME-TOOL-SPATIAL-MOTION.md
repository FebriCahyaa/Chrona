# Phase 3E — Time Tool Spatial Motion

Scope: Timer and Stopwatch state-driven spatial choreography.

## Changes

- Centralize visual lifecycle states in `ChronaTimeToolMotionState`.
- Animate Timer transitions between idle, running, paused, and completed.
- Animate Stopwatch transitions between idle, running, and paused.
- Add dedicated state-surface motion keys.
- Reuse `ChronaMotionTokens` for spatial duration/easing.
- Add pure JVM tests for lifecycle-to-motion-state mapping.

## Non-goals

- No changes to `ChronaTimeEngine` behavior.
- No changes to timer scheduling or persistence.
- No route changes.
- No deletion of existing screens.
