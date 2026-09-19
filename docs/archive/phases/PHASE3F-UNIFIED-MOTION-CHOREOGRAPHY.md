# Phase 3F — Unified Motion Choreography

## Scope
Unify destination-level enter/exit motion with the Phase 3 shared-bounds contract for the primary Chrona time tools.

## Changes
- Add `ChronaNavigationMotion` for consistent destination entry, exit, pop-entry and pop-exit transitions.
- Use low-amplitude fade/scale so shared bounds remain the spatially dominant motion.
- Attach dashboard motion keys to the destination root surfaces for Alarm, Timer and World Clock.
- Keep Stopwatch's existing destination-level dashboard motion contract.
- Preserve city-specific World Clock motion keys for future detail transitions.
- Add unit coverage for motion token relationships and stable motion keys.

## Non-goals
- No route rename or route deletion.
- No changes to repositories, schedulers, ViewModels, or `ChronaTimeEngine`.
- No destructive cleanup of existing screens.
