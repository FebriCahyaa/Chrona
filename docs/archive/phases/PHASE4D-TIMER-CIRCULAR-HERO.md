# Phase 4D — Timer Circular Hero + Dynamic Scaling

## Scope

Rebuild the Timer destination around the selected **Circular Hero with Dynamic Scaling** concept.

## Implemented

- Circular hero is the dominant visual surface in the Timer editor and countdown states.
- Hero size adapts to available width and remains bounded for compact and expanded layouts.
- Ring stroke scales with the hero size.
- Countdown typography changes between large, medium, and compact display styles based on hero scale.
- Existing Timer state choreography remains driven by `ChronaTimeToolMotionState` and `ChronaMotionTokens`.
- Editor mode and countdown mode share the existing `TIMER_EDITOR` spatial identity.
- Numeric keypad and quick-duration controls remain available in editor state.
- Completed state exposes a real Reset action with the Refresh icon.

## Preserved Architecture

- `TimerViewModel`
- `ChronaTimeEngine`
- Timer persistence and Android scheduler
- Existing Navigation route and back behavior
- Existing shared-transition contracts

## Exit Criteria

- Circular hero is visually dominant.
- Hero scales without hard-coded single-device sizing.
- Timer editing remains accessible without slider-only interaction.
- Running, paused, and completed behavior remains unchanged at the data/engine layer.
