#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

# The emulator is provisioned by android-emulator-runner; this script runs the
# instrumentation suite only after the Android 17 device reports boot complete.
./gradlew --no-daemon --stacktrace connectedDebugAndroidTest
