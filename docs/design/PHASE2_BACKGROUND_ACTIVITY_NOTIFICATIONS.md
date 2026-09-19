<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Phase 2 — Background Activity Notifications

## Baseline

Built on main revision `6f80b7f472d8959456f667a3cdd690ffda50be1f` after the
persistent clock-display and smooth analog-rendering work.

## Changed

- Added an ongoing Stopwatch notification while the stopwatch is running.
- Added Pause and Lap actions to the Stopwatch notification.
- Added an ongoing Timer countdown notification while the timer is running.
- Added Pause and Reset actions to the active Timer notification.
- Reused Android's system Chronometer/Chronometer countdown so notification
  time updates do not require a high-frequency app-side notification loop.
- Separated active Timer notifications onto a low-importance channel while
  preserving the existing high-importance completion channel.
- Notification action receivers restore the durable timer/stopwatch snapshot
  before applying a command, so actions remain functional after process death.
- Kept the application-scoped ChronaTimeEngine as the in-app timing source and
  suspended its high-frequency ticker after a notification-only background
  command until the Activity becomes visible again.
- Added no new foreground service for live notifications; the notification's
  built-in system chronometer/countdown renders elapsed/remaining time while
  BroadcastReceiver actions handle user commands.

## Preserved

- Existing timer AlarmManager expiry scheduling.
- Existing TimerService completion alert.
- Existing StopwatchRepository and TimerRepository persistence contracts.
- Existing ChronaTimeEngine monotonic timing semantics.
- Existing notification permission flow and app navigation.

## Design boundary

The ongoing notifications use standard Android notification templates. The final
visual presentation may differ across Android/OEM System UI implementations,
while notification content, actions, timing anchors, and behavior remain owned
by Chrona.

## Validation

- Pure notification timing math has JVM unit coverage.
- Source/resource/manifest structural checks should be run before merge.
- Full Gradle test/lint/build remains dependent on the repository's pinned
  networked CI toolchain when Gradle 9.7.1 is unavailable locally.
