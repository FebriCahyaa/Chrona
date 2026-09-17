<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Chrona Architecture

Chrona is a layered Kotlin + Java + C++ Android clock application. UI state is kept separate from persistence, timing engines, and Android background lifecycle so the clock remains correct when the app leaves the foreground.

## Architecture

```text
Compose UI
    │
    ▼
Feature ViewModels
    │
    ▼
Repositories / Gateways
    │
    ├── SharedPreferences persistence
    ├── AlarmManager
    ├── Foreground alert services
    └── Java / JNI time + math boundary
                       │
                       ▼
                    C++ core
```

## Feature boundaries

### Clock

`ChronaTimeEngine`, `NativeClock`, and `ClockTimeMath` provide the time/math boundary used by the clock UI; the Java engine owns shared formatting while the native bridge supplies deterministic primitives with a Java fallback. Java owns Android time access and the JNI boundary; C++ remains a small deterministic primitive layer.

### Alarm

The alarm lifecycle is intentionally split into short-lived and long-lived responsibilities:

```text
AlarmManager
    ↓
AlarmReceiver
    ↓
AlarmService
    ├── foreground notification
    ├── ringtone / vibration
    └── AlarmStateManager
            ├── one-shot → disable + cancel
            └── repeating → schedule next occurrence
```

`BootReceiver` also reconciles persisted alarms after boot, locale/time changes, package replacement, and exact-alarm permission state changes.

### Timer

```text
TimerViewModel
    ├── TimerRepository
    └── TimerSchedulerGateway
             ↓
        AlarmManager
             ↓
        TimerReceiver
             ↓
        TimerService
             └── completion alert lifecycle
```

The timer state and end timestamp are persisted so the visible UI is not the source of truth for whether a timer is active.

### Stopwatch

```text
StopwatchViewModel
       ↓
StopwatchRepository
       ↓
SharedPreferences
```

The ViewModel uses `SystemClock.elapsedRealtime()` for running-time measurement and persists checkpoints so normal process death does not silently reset the stopwatch. A reboot is treated conservatively: the previously running stopwatch is restored as paused rather than fabricating elapsed time across a clock reset.

### World Clock

World Clock cities and favorites are persisted separately from the UI. City identity is based on timezone ID, preventing duplicate entries for the same timezone.

## Dependency direction

```text
UI
 ↓
ViewModel
 ↓
Repository / Gateway
 ↓
Android implementation
 ↓
Java / JNI boundary
 ↓
C++ primitives
```

`AppContainer` is the manual dependency composition root. Android-instantiated components such as receivers and services read their dependencies from the application container because Android constructs them reflectively.

## Design rules

1. UI code does not own durable alarm, timer, stopwatch, or world-clock state.
2. Receivers perform only short entry-point work and delegate long-running behavior to services or persistent schedulers.
3. One-shot and repeating alarm state transitions are explicit and testable.
4. Exact-alarm capability is checked before scheduling; Android 12–32 may require special access, while newer releases can use the clock/timer exact-alarm capability declared by the application.
5. Native code does not own Android lifecycle state, persistence, navigation, or Compose state.
6. Source attribution and license notices are preserved when external source is incorporated; project-owned files use Chrona's project copyright notice.
