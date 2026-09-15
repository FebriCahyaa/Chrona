# Chrona — Time, Your Way

Chrona is a modern Android clock application built around a calm, premium Material 3 visual language. The rebuild combines **Kotlin + Jetpack Compose**, **Java platform bridges**, **XML Android resources**, and a small **C++20 native clock kernel**.

> Design direction: minimal, warm, expressive, glass-aware, and information-first. The visual language is inspired by modern Google Clock / Material 3 principles without copying proprietary assets or screens.

## Highlights

| Area | Implementation |
| --- | --- |
| Main clock | Live analog + split digital clock |
| Analog hands | Real device time, second-aligned updates |
| UI | Jetpack Compose + Material 3 components |
| Themes | Light / Dark / Glass |
| Navigation | Clock, World, Timer, Stopwatch, Alarm |
| Native | C++20 angle kernel via JNI |
| Java | JNI boundary + widget provider + deterministic fallback math |
| XML | Adaptive icon layers, monochrome icon, widget metadata/layout |
| Min SDK | 26 |
| Target / Compile | 37 |
| Build | AGP 9.4.0 / Gradle 9.6.1 / JDK 17 / NDK 30.0.16248370 |

## Architecture

```text
Compose UI
   │
   ├── LiveAnalogClock.kt
   │       │
   │       └── lifecycle-aware 1 Hz ticker
   │              │
   │              ▼
   │       ChronaNativeBridge.java
   │              │
   │              ▼
   │       chrona_clock.cpp (C++20)
   │              │
   │              └── hour/minute/second angles
   │
   ├── ClockTimeMath.java (fallback + JVM tests)
   └── Android XML resources
           ├── adaptive icon: background/foreground/monochrome
           └── widget metadata/layout
```

## Live analog clock and battery strategy

The analog clock is **not** a busy infinite animation loop. When the screen is RESUMED, it computes the next hand position and sleeps until the next second boundary. When the Activity is paused/stopped, the lifecycle-aware loop is suspended. The visual therefore updates once per second while visible, with no foreground service and no permanent background timer.

The native C++ routine is deliberately tiny: it only converts epoch milliseconds + timezone offset into three angles. Heavy UI work, persistence, and navigation remain in the managed Android/Compose layer.

## Adaptive app icon

`res/mipmap-anydpi-v26/ic_launcher.xml` contains the color layers, while `res/mipmap-anydpi-v33/ic_launcher.xml` adds the themed monochrome layer:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

The v33 resource supplies the monochrome layer for themed icons on newer Android versions. Android launchers apply their own mask and launcher effects to adaptive icons.

### Important limitation: a continuously rotating launcher icon

A normal Android launcher does **not** provide an API that lets an application continuously mutate its installed adaptive icon every second. Adaptive icon animations are system/launcher visual effects, not a per-second app-controlled canvas. Therefore Chrona keeps the launcher icon intentionally static and puts live time into the in-app clock and the supported home-screen widget surface.

A continuously rotating icon can only be guaranteed in a custom launcher that renders the icon itself, or through launcher/OEM-specific private behavior. Chrona does **not** use a battery-heavy foreground service merely to fake a rotating app icon.

## CI/CD

GitHub Actions is split into quality, build, and dependency graph jobs. The pipeline uses pinned major releases of the maintained GitHub/Gradle actions, explicit JDK/Android SDK configuration, NDK/CMake installation, Gradle caching, lint, unit tests, and release artifact upload.

Recommended secrets are not required for debug builds. Release signing can be added later through repository Actions secrets without changing source layout.

## Local build

```bash
# Recommended: Android Studio or a shell with JDK 17 + Android SDK 37 + NDK 30.0.16248370 + CMake 3.31.6

./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
./gradlew assembleRelease
```

## Project layout

```text
Chrona/
├── app/
│   ├── src/main/java/com/febricahyaa/clockapp/
│   │   ├── core/          # Java native bridge + fallback math
│   │   ├── ui/            # Compose screens/components/themes
│   │   ├── alarm/         # Alarm subsystem
│   │   └── widget/        # Home-screen widget provider
│   ├── src/main/cpp/      # C++20 native clock kernel
│   ├── src/main/res/      # XML themes, strings, adaptive icon, widget
│   └── build.gradle.kts
├── .github/workflows/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## Design principles

1. **Time is the hero.** The home screen gives the analog clock visual priority while the digital clock remains the precise, glanceable readout.
2. **Expressive geometry.** Thick capsules, rounded surfaces, restrained borders, and a warm accent create the Material 3 expressive feel.
3. **Glass is a system, not a blur filter.** The Glass theme uses layered translucency, contrast-aware borders, and depth instead of placing blur everywhere.
4. **Native only where it earns its place.** C++ handles deterministic math; Android business logic stays in Kotlin/Java for maintainability.
5. **Battery-aware live updates.** UI ticks are lifecycle-bound and aligned to second boundaries.

## Status

This release is the **Chrona UI/native foundation v0.3.0**. The source is structured for continued visual refinement and feature expansion rather than a placeholder/demo-only implementation.
