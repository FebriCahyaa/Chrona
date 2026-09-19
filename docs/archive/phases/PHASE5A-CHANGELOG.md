# Phase 5A Changelog

## Added
- ChronaWallClock abstraction.
- Shared ChronaTimeEngine wall-clock StateFlow.
- Compose LocalChronaTimeEngine provider.
- ChronaTimeEngine focused tests.

## Changed
- Engine pulse cadence no longer waits for 1-second delays.
- ClockDisplay, AnalogClock, World Clock and Settings preview consume the shared clock flow.
- Timer and Stopwatch ViewModels use the shared engine clock APIs.

## Preserved
- AlarmManager epoch-based scheduling for durable background triggers.
- Existing navigation, repositories, and persistence contracts.
