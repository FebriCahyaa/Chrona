#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

# The emulator is provisioned by android-emulator-runner; this script runs the
# instrumentation suite only after the Android 17 device reports boot complete.
ADB="${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}/platform-tools/adb"
if [[ ! -x "$ADB" ]]; then
  ADB="$(command -v adb)"
fi

"$ADB" devices -l
"$ADB" shell getprop ro.build.version.sdk
"$ADB" shell getprop sys.boot_completed

./gradlew --no-daemon --stacktrace --max-workers=2 connectedDebugAndroidTest
