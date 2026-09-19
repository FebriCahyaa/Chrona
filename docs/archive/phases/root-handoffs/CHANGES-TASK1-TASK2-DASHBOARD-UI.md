# Chrona — Task 1 + Task 2 + Dashboard/UI Runtime Fix

## Fixed
- Serialized AlarmViewModel load/mutations with a Mutex.
- Persisted alarm JSON with SharedPreferences `commit()` on `Dispatchers.IO`.
- Serialized WorldClockViewModel load/mutations and favorite updates.
- Persisted World Clock data and favorites with `commit()` on `Dispatchers.IO`.
- Hoisted Alarm, World Clock, Timer, and Stopwatch state collection to the ClockApp root so Dashboard and destination screens consume the same live state.
- Connected favorite World Clock cities to the Dashboard card summary.
- Replaced Android `Uri.encode()` in the JVM-tested World Clock route builder with Java URL encoding.
- Added a regression test for reserved characters and spaces in World Clock route arguments.
- Removed NavHost fade/scale transitions that caused full-screen destination ghosting/overlap; shared-transition infrastructure remains available for later refinement.

## Not removed
- No legacy XML/layout/drawable files were deleted.
- No deprecated APIs were removed in this patch.
- No GitHub Actions workflows were changed.
- No product features were intentionally removed.

## Verification
- JVM route encoding checks passed independently with Kotlin/JVM.
- `git diff --check`-style whitespace validation passed on the generated source diff.
- Full Gradle test/build could not be executed in this environment because the Gradle 9.7.1 distribution was not cached and `services.gradle.org` was unreachable.
