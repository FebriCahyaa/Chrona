<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Chrona Architecture

## Responsibility map

```text
UI / Compose Material 3
        │
        ▼
ViewModels / StateFlow
        │
        ▼
Repository interfaces
        │
 ┌──────┼───────────────────┐
 ▼      ▼                   ▼
Room +  Android APIs     Time engine
DataStore
 │      │                   │
 │      ├─ AlarmManager     ├─ wall clock
 │      ├─ Location APIs    ├─ monotonic clock
 │      ├─ Services         └─ lifecycle ticker
 │      └─ Receivers
 │
 └───────────────┐
                 ▼
          platform/native
                 │
          Java JNI bridge
                 │
              C++20
```

## Kotlin-first boundary

Kotlin owns:

- feature state,
- UI events,
- repositories and persistence orchestration,
- navigation,
- coroutine/Flow concurrency,
- Android platform coordination.

The UI consumes immutable snapshots from ViewModels and emits explicit callbacks. Business state is not copied independently into every screen.

## Native boundary

`nativelayer/ChronaNativeBridge.java` is the only Java JNI registration boundary.

```text
Kotlin NativeClock / NativeAudioEngine
              ↓
       Java ChronaNativeBridge
              ↓ JNI
        C++ chrona_clock
          ├── chrona_time
          └── chrona_audio / AAudio
```

The native layer is deliberately small. Android-owned facilities such as vibration, notifications and Activity/Service lifecycle stay in the Android layer rather than being duplicated in C++.

## Alarm architecture

```text
AlarmViewModel
    ↓
AlarmRepository
    ↓
AlarmSchedulerGateway
    ↓
AlarmManager.setAlarmClock()
    ↓
AlarmReceiver
    ↓
AlarmService (foreground)
    ├── AlarmNotificationFactory
    ├── AlarmStateManager
    └── AlarmSoundGateway
          ├── Android Ringtone
          └── AAudio fallback
```

`BootReceiver` reconciles the persisted schedule after boot, timezone/time changes, app replacement and exact-alarm permission changes.

## Timer and Stopwatch

Timer uses the same scheduling model as the alarm feature and retains durable completion/recovery state. Its running notification delegates countdown rendering to `setChronometerCountDown(true)`, while completion uses an exact alarm path, a high-priority full-screen intent, audio focus, and haptic feedback.

Stopwatch uses the shared monotonic engine and Android's notification chronometer rather than a background Compose ticker. When actively running it can be kept alive through `StopwatchService` as a foreground service with the `specialUse` type and an explicit subtype declaration.

## World Clock architecture

```text
TimeZoneCatalog ───────┐
                       ▼
                 WorldClockViewModel
                       │
                       ├── items
                       └── favorites
                       │
CurrentLocationVM ─────┼── location / loading / error / requestId
                       ▼
               WorldClockScreen
                 ├── Hero
                 ├── Location
                 ├── Favorite grid
                 ├── Additional list
                 └── Fixed map
```

## Settings architecture

`ClockSettings` is an immutable persisted model. `SettingsViewModel` owns mutation and persistence. `SecondsDisplayMode` therefore survives process death and configuration changes instead of existing only as Compose-local state.

## OS configuration resources

`res/xml/` is used only where the Android framework needs declarative metadata:

- widget provider metadata,
- static shortcuts,
- backup/data extraction policy,
- network security policy.

Compose screen layout is not duplicated into XML merely for the sake of using XML.

## Source boundaries

- `di/`: Hilt bindings and providers.
- `data/local/`: Room entities, DAOs and structured repositories.
- `data/`: DataStore repositories and one-time legacy migration.
- `data/security/`: Android Keystore secret boundary.
- `ipc/`: internal Binder/AIDL service boundary.
- `nativelayer/`: JNI boundary to the native `.so`.
- `telemetry/`: observability abstraction with OSS no-op and Play Firebase implementation.
- `update/`: WorkManager scheduling and maintenance.

## Annotation Processing Boundary

Chrona uses Kotlin Symbol Processing (KSP) as its only annotation-processing path for the Android application. The `org.jetbrains.kotlin.kapt` plugin and `kapt` configurations are intentionally absent because Android Gradle Plugin 9 built-in Kotlin is incompatible with the Kotlin KAPT plugin. Hilt's KSP backend is used with Dagger/Hilt 2.60.1, Room uses its KSP compiler, and `androidx.hilt:hilt-compiler:1.4.0` provides the KSP processor for `@HiltWorker`. The KSP plugin is pinned independently at 2.3.11 while Kotlin remains 2.4.10.
