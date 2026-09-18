#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

pass() { echo "[PASS] $*"; }
skip() { echo "[SKIP] $*"; }
fail() { echo "[FAIL] $*" >&2; exit 1; }

./scripts/phase6-release-gate.sh
pass "Phase 6A static gate"

if grep -RIn --exclude-dir=.git --exclude='*.patch' 'delay(1000)' app/src/main >/dev/null 2>&1; then
  fail "fixed one-second UI delay remains in app source"
fi
pass "no fixed one-second UI delay in app source"

grep -Fq 'ChronaRuntimeLifecycleEffect(container.timeEngine)' app/src/main/java/com/febricahyaa/clockapp/ClockApp.kt \
  || fail "runtime lifecycle binding is not installed at app root"
pass "runtime lifecycle binding"

grep -Fq 'androidx.compose.ui:ui-test-junit4' app/build.gradle.kts \
  || fail "Compose instrumentation test dependency is missing"
pass "runtime smoke-test dependency"

if ! command -v adb >/dev/null 2>&1; then
  skip "adb is unavailable; connected-device execution must be performed in Android CI or on a local device"
  exit 0
fi

DEVICE_COUNT="$(adb devices | awk 'NR>1 && $2=="device" {count++} END {print count+0}')"
if [[ "$DEVICE_COUNT" -eq 0 ]]; then
  skip "no authorized Android device/emulator is connected"
  exit 0
fi

if [[ ! -x ./gradlew ]]; then
  skip "gradlew is not executable in this checkout; use bash ./gradlew on local environments"
  bash ./gradlew connectedDebugAndroidTest --no-daemon
else
  ./gradlew connectedDebugAndroidTest --no-daemon
fi

pass "connectedDebugAndroidTest"
