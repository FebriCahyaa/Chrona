#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

./gradlew --no-daemon --stacktrace connectedDebugAndroidTest
