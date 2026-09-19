#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ADB="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}/platform-tools/adb"

if [[ ! -x "$ADB" ]]; then
  ADB="$(command -v adb)"
fi

SERIAL="${ANDROID_SERIAL:-emulator-5554}"
export ANDROID_ADB_SERVER_PORT="${ANDROID_ADB_SERVER_PORT:-5037}"

adb_state() {
  "$ADB" -s "$SERIAL" get-state 2>/dev/null || true
}

boot_state() {
  "$ADB" -s "$SERIAL" shell getprop sys.boot_completed 2>/dev/null \
    | tr -d '\r' || true
}

echo "== ADB VERSION =="
"$ADB" version

echo "== START ADB SERVER =="
"$ADB" start-server

echo "== ADB DEVICES =="
"$ADB" devices -l

echo "== WAIT FOR DEVICE =="
device_deadline=$((SECONDS + 300))
device_state=""

while (( SECONDS < device_deadline )); do
  device_state="$(adb_state)"
  echo "ADB state: ${device_state:-unknown}"

  if [[ "$device_state" == "device" ]]; then
    break
  fi

  if [[ "$device_state" == "offline" ]]; then
    "$ADB" reconnect offline >/dev/null 2>&1 || true
    "$ADB" start-server >/dev/null 2>&1 || true
  fi

  sleep 2
done

device_state="$(adb_state)"
if [[ "$device_state" != "device" ]]; then
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
boot_deadline=$((SECONDS + 120))
boot_completed=""

while (( SECONDS < boot_deadline )); do
  boot_completed="$(boot_state)"
  echo "sys.boot_completed=${boot_completed:-unknown}"

  if [[ "$boot_completed" == "1" ]]; then
    break
  fi

  sleep 2
done

if [[ "$boot_completed" != "1" ]]; then
  echo "::error::Android emulator reached ADB 'device' state but boot did not complete within 120 seconds."
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
