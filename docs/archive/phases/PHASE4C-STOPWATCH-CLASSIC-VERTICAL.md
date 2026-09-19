# Phase 4C — Stopwatch Classic Vertical

Phase 4C rebuilds the Stopwatch destination around the selected **Classic Vertical** concept.

## Goals

- Keep the elapsed-time surface visually dominant.
- Use an explicit `weight(1f)` layout contract for the primary time surface.
- Keep lap history in a separate bounded vertical surface when data exists.
- Keep actions fixed at the bottom and easy to reach.
- Preserve the Phase 3 shared-transition keys and ChronaTimeEngine integration.

## Behavior

The destination is now organized as:

1. Primary stopwatch time surface (`weight(1f)`).
2. Lap history (`weight(1f)`) when laps exist.
3. Fixed action row at the bottom.

Lap recording is disabled while the stopwatch is not running. Existing ViewModel, repository, persistence, scheduling, navigation, and engine contracts are unchanged.
