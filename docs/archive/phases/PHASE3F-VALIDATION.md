# Phase 3F Validation

## Static validation
- Required Phase 3F source and test files present.
- Navigation graph references all four navigation motion transitions.
- Alarm, Timer, World Clock, and Stopwatch expose the dashboard motion contract.
- Dashboard motion keys are not duplicated as both destination root and child card for Alarm/Timer/World Clock.
- `git diff --check` cannot be run because this source archive is not a Git worktree.

## Build validation
- Full Gradle build/test was not performed in the archive workspace.
- Run `./gradlew :app:testDebugUnitTest` and `./gradlew :app:assembleDebug` in the project environment/CI.
