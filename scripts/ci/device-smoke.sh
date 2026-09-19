#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ADB="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}/platform-tools/adb"

if [[ ! -x "$ADB" ]]; then
  ADB="$(command -v adb)"
fi

SERIAL="${ANDROID_SERIAL:-emulator-5554}"
export ANDROID_ADB_SERVER_PORT="${ANDROID_ADB_SERVER_PORT:-5037}"

echo "== ADB VERSION =="
"$ADB" version

echo "== START ADB SERVER =="
"$ADB" start-server

echo "== ADB DEVICES =="
"$ADB" devices -l

echo "== WAIT FOR DEVICE =="

deadline=$((SECONDS + 300))

while (( SECONDS < deadline )); do
  state="$("$ADB" -s "$SERIAL" get-state 2>/dev/null || true)

  echo "ADB state: ${state:-unknown}"

  if [[ "$state" == "device" ]]; then
    break
  fi

  if [[ "$state" == "offline" ]]; then
    "$ADB" start-server >/dev/null 2>&1 || true
  fi

  sleep 2
done

if [[ "$("$ADB" -s "$SERIAL" get-state 2>/dev/null || true)" != "device" ]]; then
  echo "::error::Android emulator did not become ready within 300 seconds."
  echo "== FINAL ADB DEVICES =="
  "$ADB" devices -l || true
  echo "== EMULATOR PROCESSES =="
  pgrep -a emulator || true
  pgrep -a qemu || true
  echo "== KVM =="
  ls -la /dev/kvm || true
  exit 1
fi

echo "== BOOT STATUS =="

boot_completed="$(
  "$ADB" -s "$SERIAL" shell getprop sys.boot_completed |
  tr -d '\r'
)"

echo "sys.boot_completed=$boot_completed"

if [[ "$boot_completed" != "1" ]]; then
  echo "::error::Android emulator connected but boot is not complete."
  "$ADB" -s "$SERIAL" shell getprop ro.build.version.sdk || true
  "$ADB" -s "$SERIAL" shell getprop ro.build.version.release || true
  exit 1
fi

echo "== SDK VERSION =="

"$ADB" -s "$SERIAL" shell getprop ro.build.version.sdk

echo "== PAGE SIZE =="

"$ADB" -s "$SERIAL" shell getconf PAGE_SIZE || true

echo "== RUN INSTRUMENTATION =="

timeout 10m \
  ./gradlew \
  --no-daemon \
  --stacktrace \
  --max-workers=2 \
  connectedDebugAndroidTest
