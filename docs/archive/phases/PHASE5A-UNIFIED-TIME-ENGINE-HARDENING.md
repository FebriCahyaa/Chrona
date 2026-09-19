# Phase 5A — Unified Time Engine Hardening

- Shared application-scoped ChronaTimeEngine now exposes both monotonic and wall-clock reads.
- Foreground clock surfaces consume the shared wall-clock StateFlow instead of creating independent 1-second polling loops.
- Timer and Stopwatch ViewModels no longer read platform clocks directly for foreground timing.
- Runtime pulse rates are 50ms for active stopwatch, 100ms for active timer, and 250ms for shared wall-clock display updates.
- No `delay(1000)` loop remains in foreground time UI or engine code.
- AlarmManager persistence/scheduling continues to use wall-clock epoch millis where Android requires it.
