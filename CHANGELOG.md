<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Changelog

Chrona release notes are evidence-based. Completed entries describe changes that exist in the repository source; build and release claims are added only after the corresponding CI gates succeed.

## Unreleased — Foundation / Build System

### 🏗️ Foundation

- Added `AlarmService` so alarm playback and foreground lifecycle no longer depend on a `BroadcastReceiver` remaining alive.
- Added `AlarmStateManager` for explicit one-shot disable and repeating-alarm rescheduling transitions.
- Extended alarm recovery to boot, locale, time, timezone, package replacement, and exact-alarm permission-state changes.
- Added persistent Timer state and a dedicated timer scheduler/service path.
- Added persistent Stopwatch state and recovery-safe elapsed-time checkpoints.
- Persisted World Clock favorites alongside the user's saved city list.
- Extracted alarm trigger-time calculation into a testable `AlarmTimeCalculator`.

### ⚙️ Build System

- Kept Debug, test, lint, native, and Release responsibilities separated in GitHub Actions.
- Release signing remains isolated to the `release` GitHub Environment and is not stored in the repository.
- Release artifacts are designed to be verified with `apksigner` and accompanied by a SHA-256 checksum before publication.

### 🔐 Security

- Release keystore material remains outside the checked-out repository and is supplied through GitHub Environment Secrets.
- No production secret is introduced into the new alarm/timer/stopwatch lifecycle code.

### ⚠️ Known Issues

- Full Android compilation and device-level lifecycle testing still require a networked Android build host.
- Physical-device validation is still required for lock-screen alarm presentation, ringtone behavior, vendor background restrictions, and reboot/time-change edge cases.
