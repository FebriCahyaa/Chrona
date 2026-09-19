#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail

if ! command -v adb >/dev/null; then
  echo 'adb is required.' >&2
  exit 1
fi

adb shell getprop ro.build.version.release || true
adb shell dumpsys activity service com.android.server.timezone.TimeZoneDetectorService 2>/dev/null | head -n 20 || true
printf '%s\n' 'Chrona reads timezone rules from the Android ICU/tzdata runtime; no static zone database is vendored.'