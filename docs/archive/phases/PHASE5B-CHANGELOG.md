# Phase 5B Changelog

## Added

- `TimerDurabilityPolicy` and pure recovery tests.
- `AlarmTriggerPolicy` and stale-event tests.
- Explicit alarm snooze intent metadata.

## Changed

- Timer completion is now durably marked before notification/foreground-service side effects.
- Boot and app restore paths share one timer recovery policy.
- Timer state persistence precedes scheduler synchronization on user actions.
- Alarm receivers and services revalidate alarm state before ringing.
- Alarm notification actions commit trigger state before stopping the service.

## Removed

- No files removed.
