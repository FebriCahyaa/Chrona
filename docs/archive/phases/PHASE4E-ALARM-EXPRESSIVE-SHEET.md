# Phase 4E — Alarm Expressive Sheet Editor

Status: DONE

## Scope

Refine Alarm create/edit into a full expressive Material 3 sheet while preserving the existing AlarmViewModel, AlarmRepository, scheduler gateway, navigation route, and Phase 3 shared-motion contract.

## UI changes

- Full-height `ModalBottomSheet` presentation with a stable drag handle.
- Fixed sheet action bar for Cancel / Save or Create.
- Scrollable editor content for small displays and IME-safe label editing.
- Large time preview paired with the existing `TimePicker`.
- Schedule section for label and weekday repetition.
- Alert behavior section for ringtone, vibration, and enabled state.
- Shared transition remains keyed by `ChronaMotionKeys.ALARM_EDITOR`.

## Behavior guarantees

- Create mode uses a generated ID only when the user commits.
- Edit mode preserves the existing alarm ID.
- Existing repository and scheduler mutation flow is unchanged.
- Cancel/dismiss discards unsaved local editor changes.
- Existing list card expansion, inline ringtone updates, toggle, edit, and delete behavior remain available.

## Compatibility cleanup

- Removed an accidental literal `\\n` source encoding from the previous AlarmScreen artifact.
- Defaulted the legacy `onEdit` callback to preserve source compatibility with existing callers.

## Exit criteria

- Expressive sheet implemented.
- Source audit passes.
- No source files deleted.
- Patch and full-source archive produced.
- Full Android compilation remains dependent on the configured build environment.
