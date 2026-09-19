# Phase 4F — Settings Adaptive Dashboard

Status: DONE

## Scope

Recompose Settings as an adaptive dashboard without changing the existing persistence or update flows.

## Layout policy

- Compact: one-column vertical flow optimized for phones.
- Medium: live preview followed by two dashboard columns.
- Expanded: three dashboard columns separating clock, appearance, and operational/app information.
- Content remains vertically scrollable at the destination level.
- Maximum content width is bounded to 1180dp to avoid excessive line lengths on wide displays.

## Preserved behavior

- SettingsViewModel remains the source of persisted settings.
- Notification settings continue routing to Android system settings.
- Update checks continue using UpdateUiState and the existing updater pipeline.
- Legal navigation continues using the existing destination.

## Architecture

`ChronaSettingsWindowClass` is a pure layout policy, keeping width breakpoints out of business state and making the responsive decision unit-testable.
