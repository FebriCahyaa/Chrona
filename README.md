<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Chrona

[![Chrona Debug Build](https://github.com/FebriCahyaa/Chrona/actions/workflows/debug-build.yml/badge.svg)](https://github.com/FebriCahyaa/Chrona/actions/workflows/debug-build.yml)
[![Chrona Pull Request](https://github.com/FebriCahyaa/Chrona/actions/workflows/pull-request-issue.yml/badge.svg)](https://github.com/FebriCahyaa/Chrona/actions/workflows/pull-request-issue.yml)

**A calm, precise Android time workspace.**

Chrona is a Kotlin-first Android clock application built with Jetpack Compose Material 3, Kotlin Coroutines/Flow, Android system scheduling APIs, Android ICU/IANA timezone data, and a small C++20/JNI layer for deterministic timing/math and low-latency alert audio.

## Feature surface

- 🕒 Local clock with digital/analog presentation and 12/24-hour formatting.
- 🌍 World Clock with dynamic IANA/ICU timezone catalog, foreground Current Location, favorites, city search, and a fixed world map.
- ⏰ Alarm with exact Android scheduling, alarm-clock semantics, reboot/timezone reconciliation, and a foreground ringing service.
- ⏱️ Timer with durable recovery, exact scheduling, SystemUI countdown chronometer, full-screen completion UI, audio focus and haptic alerting.
- ⏱️ Stopwatch backed by the shared Chrona time engine and an ongoing system chronometer notification.
- 🎛️ Material 3 settings with persistent seconds-display modes.
- 📱 Android App Widget and launcher shortcuts.

## Core architecture

```text
                 ┌───────────────────────────────┐
                 │ Jetpack Compose Material 3 UI │
                 └───────────────┬───────────────┘
                                 │ StateFlow / events
                 ┌───────────────▼───────────────┐
                 │ Feature ViewModels             │
                 │ Alarm / Timer / Stopwatch     │
                 │ World Clock / Settings / etc. │
                 └───────────────┬───────────────┘
                                 │ repository interfaces
                 ┌───────────────▼───────────────┐
                 │ Data + Android platform layer  │
                 │ Room / DataStore / WorkManager  │
                 │ AlarmManager / Services / Geo  │
                 └───────┬─────────────────┬──────┘
                         │                 │
                 ┌───────▼───────┐  ┌────▼──────────┐
                 │ Chrona Time   │  │ C++20 / JNI   │
                 │ Engine         │  │ timing / math │
                 └───────────────┘  │ AAudio alert  │
                                   └───────────────┘
```

### Kotlin

Kotlin owns feature logic, UI state, persistence orchestration, navigation and asynchronous work. Coroutines and Flow are the concurrency boundary; Compose reads immutable state and emits user intents. Hilt owns construction and graph composition; Room is the structured local database; DataStore owns lightweight preferences and durable transient state; WorkManager owns deferrable scheduled work.

### Enterprise runtime boundaries

Chrona separates platform responsibilities instead of duplicating them in UI code:

- **DI:** Hilt modules bind repository, scheduler, location, sound, service and storage interfaces.
- **Persistence:** Room is the source of truth for structured alarms/world-clock/history data; DataStore stores settings/timer/stopwatch/update metadata. A one-time migration preserves legacy SharedPreferences data.
- **Offline-first:** UI observes local `Flow` state first. Network-backed release metadata is cached locally and refreshed through WorkManager when constraints allow.
- **Security:** optional secrets use Android Keystore-backed AES-GCM storage. No production secret or Firebase configuration is committed.
- **Observability:** telemetry is an interface. OSS is no-op; Play can bind Firebase Crashlytics/JankStats when real Firebase configuration is supplied.
- **IPC:** the internal `IChronaSystemService` AIDL contract is a Binder boundary for the dedicated service process; it does not pretend to be direct hardware-driver access.
- **Native:** the C++20/AAudio shared library is loaded through the Java JNI bridge. Android owns lifecycle, notification, vibration and permissions.
- **Variants:** `oss` and `play` flavors share the same source while separating distribution-specific integrations.
- **UI/animation:** Compose Material 3, Canvas and VectorDrawable stay primary; Lottie is an optional JSON-animation adapter, not a mandatory dependency for every screen.

### Java

`ChronaNativeBridge.java` is the explicit JNI boundary. Java is intentionally kept at the platform/native boundary so JNI method registration and Android-facing type conversion remain isolated from feature code.

### C++ / NDK

The native layer is C++20 and is built through CMake. It provides deterministic timing/math primitives and an AAudio low-latency fallback tone for alert playback when the normal Android ringtone path is unavailable. Android's `Vibrator`/`VibrationEffect` remains the platform haptic boundary; haptics are not falsely represented as native when the OS API is the actual owner.

## World Clock

The World Clock has one scrolling composition and one state model:

```text
Hero clock
    ↓
Current Location
    ↓
Saved cities
    ├── Favorites 1–4 → 2×2 grid
    └── Additional → full-width cards
    ↓
Fixed World Map
    ↓
Material 3 navigation bar
```

The city catalog uses the runtime timezone database instead of a frozen offset table. `TimeZoneCatalog` combines `ZoneId.getAvailableZoneIds()` with Android ICU canonicalization and region mapping. Saved items use IANA zone IDs as the stable key.

Current Location is foreground-only and is not persisted as a World Clock item:

```text
GMS FusedLocationProviderClient
            ↓ fallback
Android LocationManager
            ↓
recent last-known location
            ↓
Android Geocoder
```

Permission UX is initiated from the World Clock through the Android/Jetpack Location Button. Location failure is reported as a transient Material 3 snackbar rather than a permanent dashboard panel.

The world map is intentionally fixed: no drag, pan, pinch or manual meridian manipulation. The device latitude/longitude determines the marker and the highlighted map region.

## Seconds display

The digital World Clock supports a persisted `SecondsDisplayMode` setting:

| Mode | Behavior |
| --- | --- |
| `STACKED` | Dominant hour/minute with small vertical seconds beside the main value. |
| `INLINE` | Standard `HH:MM:SS` presentation. |
| `FADING_SCROLL` | Focused current second with vertically moving, faded and blurred adjacent values. |
| `MINIMAL` | Small secondary seconds beside the dominant time. |
| `CIRCULAR` | Material 3 `CircularWavyProgressIndicator` driven by the current second. |

Circular mode maps `00..59` to a determinate progress value and uses `ProgressIndicatorDefaults.ProgressAnimationSpec`. Its accent cycle changes per minute rather than on every second.

## Android system integration

Chrona keeps OS-owned behavior in OS-facing components:

- `AlarmManager.setAlarmClock()` for alarm-clock scheduling.
- `USE_EXACT_ALARM` / exact-alarm compatibility handling.
- Foreground services for ringing/completion experiences that remain active outside the main UI.
- Broadcast receivers for boot, timezone and time-change reconciliation.
- `appwidget-provider` metadata under `res/xml/` for the launcher widget.
- `shortcuts.xml` under `res/xml/` for static launcher shortcuts.
- `network_security_config.xml` with cleartext traffic disabled.
- `data_extraction_rules.xml` and legacy `backup_rules.xml` for controlled backup/transfer behavior.

Compose does not eliminate `res/xml`: these files are consumed by Android framework components before the Compose UI is relevant.

## Material 3 UI policy

Chrona's application UI uses Material 3 components and tokens. The repository contains the Material icon vector artifact because that artifact is where the standard Compose icon set is published; source audits reject Material 2 UI components while allowing `androidx.compose.material.icons.*` imports.

Primary UI primitives include:

- `Scaffold`, `CenterAlignedTopAppBar` and Material 3 top bars.
- `ElevatedCard`, `OutlinedCard` and `Surface` for hierarchy/elevation.
- `NavigationBar`, `NavigationBarItem` and segmented controls.
- `SnackbarHost`, `OutlinedTextField` and M3 buttons.
- `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- Compose Canvas for custom analog-clock rendering and map rendering.

## Build toolchain

| Tool | Version |
| --- | --- |
| Gradle | 9.7.1 |
| Android Gradle Plugin | 9.4.1 |
| Kotlin | 2.4.10 |
| Compose BOM | 2026.09.00 |
| Gradle runtime JDK | 25 |
| Java/Kotlin toolchain | 17 |
| Android API | 37 / Platform 37.1 |
| Build Tools | 37.0.0 |
| CMake | 3.31.5 |
| NDK | 28.2.13676358 |

## GitHub Actions

Chrona intentionally exposes exactly five workflow files:

| Workflow | Purpose |
| --- | --- |
| `update-commit.yml` | Main-branch health, source/dependency/security checks and commit summary. |
| `debug-build.yml` | Unit/lint validation and four-ABI Debug compilation. APK artifacts only on explicit manual dispatch. |
| `release-build.yml` | Manual release validation, signed APK/AAB packaging, provenance and optional GitHub Release publication. |
| `sync-source.yml` | Source, dependency and localization synchronization. |
| `pull-request-issue.yml` | Pull-request validation, dependency review, CodeQL and issue intake checks. |

Normal push/PR pipelines do not automatically publish APK or ZIP artifacts. Release binary generation is manual.

## Telegram automation

Telegram notifications are separated by concern but live inside the five main workflows rather than separate workflow files:

- CI/update notifications.
- Pull request notifications.
- Release notifications.

Dependabot is treated as a pull-request event with a distinct renderer heading. Bot tokens remain GitHub Secrets.

## Repository documentation

- [`docs/world-clock/WORLD_CLOCK_UI.md`](docs/world-clock/WORLD_CLOCK_UI.md)
- [`docs/world-clock/WORLD_CLOCK_DATA.md`](docs/world-clock/WORLD_CLOCK_DATA.md)
- [`docs/architecture/ARCHITECTURE.md`](docs/architecture/ARCHITECTURE.md)
- [`docs/ci/CI_CD.md`](docs/ci/CI_CD.md)
- [`docs/repository/REPOSITORY_STRUCTURE.md`](docs/repository/REPOSITORY_STRUCTURE.md)
- [`docs/operations/TELEGRAM_BOTS.md`](docs/operations/TELEGRAM_BOTS.md)
- [`docs/legal/DEPENDENCY_LICENSES.md`](docs/legal/DEPENDENCY_LICENSES.md)
- [`docs/localization/LOCALIZATION.md`](docs/localization/LOCALIZATION.md)

## Local verification

```bash
python3 scripts/audit/material3-only.py
python3 scripts/audit/workflow-audit.py
bash scripts/audit/source-audit.sh
./gradlew :app:testOssDebugUnitTest :app:lintOssDebug --no-daemon
./gradlew :app:assembleOssDebug -PchronaTargetAbi=arm64-v8a --no-daemon
```

On the hosted CI runner, native compilation is exercised again by the four-ABI Debug matrix.

## Licensing

Project source and documentation licensing follows [`LICENSE`](LICENSE). Third-party dependencies remain governed by their own licenses. Android platform APIs and the Android ICU/IANA timezone data consumed through the runtime are not copied into the repository as a separate timezone database.

© 2026 Febrian Rahmad Cahya.

## Architecture documents

- [Architecture](docs/architecture/ARCHITECTURE.md)
- [Storage and offline-first](docs/architecture/DATA_STORAGE.md)
- [World Clock UI](docs/world-clock/WORLD_CLOCK_UI.md)
- [World Clock data](docs/world-clock/WORLD_CLOCK_DATA.md)
- [App Links](docs/app-links/APP_LINKS.md)
- [CI/CD](docs/ci/CI_CD.md)
