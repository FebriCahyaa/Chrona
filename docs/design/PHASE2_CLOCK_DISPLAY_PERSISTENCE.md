<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Phase 2 — Persistent Clock Display + Smooth Analog Rendering

## Baseline

Built on main revision `f19f6bd852335ca35fa87234d45cd0a0a64588b3`.

## Changed

- Added a persisted `ClockDisplayMode` preference to `ClockSettings`.
- Default display remains `DIGITAL` on first launch and after a settings reset.
- Settings now exposes a Material 3 single-choice segmented control for Digital/Analog.
- Main dashboard reads the display mode from `SettingsViewModel` instead of owning an independent `rememberSaveable` mode.
- Home quick-toggle changes are persisted immediately through the existing `SettingsRepository`.
- Analog clock rendering was refined with Material 3 tonal surfaces, layered outline/ticks, refined hand proportions, and theme-aware accent treatment.
- Added a frame-sampled wall-clock projection for smooth foreground analog-second rendering while respecting the Activity lifecycle.
- Digital clock typography uses tabular numerals for stable second-by-second layout.

## Preserved

- `ChronaTimeEngine` remains the time source of truth.
- Existing timezone and 12/24-hour behavior remains unchanged.
- Existing Timer, Stopwatch, Alarm and World Clock behavior remains unchanged.
- Existing navigation and settings repository contracts remain intact.

## Deferred

Ongoing Stopwatch/Timer notification redesign remains a separate background-activity UX step so notification service types can be reviewed and tested independently from the clock-rendering work.
