# Phase 6B — Runtime / Device QA

Status: COMPLETE (automation gate; physical execution delegated to CI/device)

## Scope

Phase 6B verifies the runtime contract before physical-device execution can be performed in a
connected Android environment. It adds a minimal instrumentation smoke test, a repeatable QA
preflight script, and the manual/device matrix used for Android 17 validation.

## Automated instrumentation

`app/src/androidTest/java/com/febricahyaa/clockapp/ChronaRuntimeSmokeTest.kt` verifies:

- `MainActivity` launches and Compose can produce a root surface.
- The Activity remains usable after initial composition.

The test intentionally does not assume whether onboarding has already been completed, so it remains
valid for both fresh-install and returning-user states.

## Preflight command

```bash
./scripts/phase6-runtime-qa.sh
```

The script runs the Phase 6A release gate, checks the unified lifecycle/ticker contract, and then
runs `connectedDebugAndroidTest` when an authorized `adb` device/emulator is available. Without a
device it reports a SKIP rather than claiming device verification.

GitHub Actions also contains `.github/workflows/runtime-device-qa.yml`, which provisions an Android 17
API 37 Google APIs emulator and executes the instrumentation suite automatically for pull requests or
manual runs. That workflow is the preferred execution path when no local device is available.

## Android 17 device matrix

| Scenario | Expected result |
| --- | --- |
| Fresh install | Onboarding is shown and can be completed or skipped. |
| Notification permission granted | Permission state is reflected without repeated prompting. |
| Notification permission denied | App remains usable; settings can reopen notification settings. |
| Dashboard | Hero clock renders; primary cards navigate correctly. |
| Alarm create/edit/delete | State persists; stale deleted/disabled events do not ring. |
| Alarm snooze/dismiss | Correct action is committed before service termination. |
| World Clock search/add/detail/back | City is added, detail opens, and back returns to list. |
| Timer start/pause/resume/reset | Monotonic elapsed behavior remains correct across background/foreground. |
| Timer expiry | Completion survives lifecycle changes and alert side effects. |
| Stopwatch start/pause/lap/reset | Elapsed value and lap history remain consistent. |
| Background / foreground | UI ticker stops in background and reconciles immediately on resume. |
| Process recreation | Durable timer/alarm state is restored without duplicated events. |
| Rotation / resize | No crash, clipped controls, or inaccessible primary actions. |
| Dark/light theme | Content remains legible and surfaces retain contrast. |
| Settings updater | Latest release metadata and timeline remain readable. |
| Back navigation | Primary tools return to Dashboard; nested World Clock routes unwind locally. |

## Physical-device commands

After installing a debug APK on a device:

```bash
adb shell am force-stop com.febricahyaa.clockapp.debug
adb shell monkey -p com.febricahyaa.clockapp.debug 1
adb shell dumpsys activity activities | grep -m1 mResumedActivity
```

For process-death verification during an active Timer/Stopwatch scenario:

```bash
adb shell am force-stop com.febricahyaa.clockapp.debug
adb shell monkey -p com.febricahyaa.clockapp.debug 1
```

Then verify the elapsed state against the expected monotonic timestamp behavior.

## Execution boundary

The local environment used to prepare this phase has no `adb` or Android emulator, so it does not
claim physical-device execution. A connected-device CI job or local Android 17 device remains the
authoritative source for final runtime results. The checked-in emulator workflow provides the automated
execution path for that remaining verification.
