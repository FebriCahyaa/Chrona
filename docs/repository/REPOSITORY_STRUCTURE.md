<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Repository Structure

```text
app/src/main/java/com/febricahyaa/clockapp/
├── alarm/          # exact alarms, receivers, ringing service, sound/audio focus
├── core/           # pure clock/trigger/input helpers and app defaults
├── data/           # repository interfaces, DataStore, migration and APIs
│   ├── local/      # Room database, entities and DAOs
│   ├── location/   # location providers and reverse-geocoding boundary
│   ├── onboarding/ # onboarding persistence/state
│   ├── security/   # Android Keystore secret boundary
│   ├── timezone/   # deterministic ICU/IANA timezone catalog
│   └── update/     # release API and cached update state
├── di/             # Hilt modules and graph bindings
├── ipc/            # internal Binder/AIDL service/client boundary
├── model/          # immutable feature models
├── navigation/     # destinations, routes and navigation policy
├── nativelayer/    # Java JNI bridge + Kotlin native facades
├── notification/   # shared notification channels/math
├── stopwatch/      # stopwatch FGS, receivers and notifications
├── telemetry/      # telemetry abstraction + flavor implementations
├── time/           # shared wall/monotonic Chrona time engine
├── timer/          # timer scheduling/recovery/completion notifications
├── ui/             # Compose Material 3 screens/components/theme
│   ├── components/
│   ├── screens/
│   ├── theme/
│   ├── update/
│   └── viewmodel/
├── update/         # WorkManager scheduling and workers
└── widget/         # Android App Widget provider

app/src/main/aidl/com/febricahyaa/clockapp/ipc/
└── IChronaSystemService.aidl

app/src/main/cpp/
├── chrona_clock.cpp
├── chrona_time.cpp/.h
└── chrona_audio.cpp/.h

app/src/main/res/
├── layout/         # OS-owned RemoteViews widget layout
├── drawable/       # launcher/status vector resources
├── values*/        # strings, colors, themes
└── xml/            # framework metadata/policies + shortcuts

app/src/oss/         # FOSS/default implementation (no-op telemetry)
app/src/play/        # Play distribution implementation (Firebase telemetry)
app/src/test/        # JVM unit tests, including MockK-backed DI boundary tests
app/src/androidTest/ # Hilt graph + Compose/Espresso instrumentation tests

docs/
├── app-links/      # verified-link contract + assetlinks template
├── architecture/  # runtime/source boundary
├── ci/             # five-workflow CI/CD contract
├── operations/     # Telegram/operations documentation
├── release/        # release procedure
├── repository/     # source placement rules
└── world-clock/    # World Clock UI/data contracts

.github/
├── actions/setup-android/
├── codeql-config.yml
├── dependency-review-config.yml
└── workflows/
    ├── update-commit.yml
    ├── debug-build.yml
    ├── release-build.yml
    ├── sync-source.yml
    └── pull-request-issue.yml
```

## Placement rules

Use the smallest package that fully describes ownership. Feature state belongs in `ui/viewmodel`; persistence belongs in `data`; dependency bindings belong in `di`; Android framework entry points belong beside their feature (`alarm`, `timer`, `stopwatch`, `widget`, etc.); Binder/AIDL belongs in `ipc`; native/JNI files belong only in `nativelayer`/`cpp`; flavor-specific integrations belong in `src/oss` or `src/play`.

Do not create phase folders, patch archives, handoff documents, or generated build artifacts inside the source tree.
