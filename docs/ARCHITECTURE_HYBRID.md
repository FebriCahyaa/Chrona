# Chrona Hybrid Architecture

Chrona is intentionally implemented as a layered Kotlin + Java + C++ application.

## Kotlin

Jetpack Compose UI, navigation, state, persistence orchestration, alarm/timer/stopwatch feature state, and Android lifecycle coordination.

## Java

Platform-oriented time services and JNI boundary. `ChronaTimeEngine.java` owns formatting/offset calculations used by the UI, while `ChronaNativeBridge.java` is the stable Java boundary to native timing/math.

## C++

Native timing primitives for elapsed time, remaining timer time, and clock-hand math. JNI keeps this layer isolated from the Compose layer.

```text
Compose UI (Kotlin)
       |
Feature/state (Kotlin)
       |
Java platform time + JNI bridge
       |
Native timing/math (C++)
```

The redesign intentionally leaves GitHub Actions, Gradle toolchain declarations, SDK levels, and CI configuration untouched.
