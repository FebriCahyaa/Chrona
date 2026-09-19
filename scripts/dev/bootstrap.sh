#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT_DIR"

printf '%s\n' 'Chrona development environment'
printf '%s\n' '--------------------------------'
printf 'Java runtime: '
java -version 2>&1 | head -n 1 || true
printf 'Gradle: '
./gradlew --version 2>/dev/null | awk '/Gradle / {print $2; exit}' || true
printf 'Android SDK: %s\n' "${ANDROID_SDK_ROOT:-${ANDROID_HOME:-not configured}}"
printf '\nRecommended checks:\n'
printf '%s\n' '  ./scripts/audit/source-audit.sh'
printf '%s\n' '  ./gradlew testDebugUnitTest'
printf '%s\n' '  ./gradlew lintDebug'
printf '%s\n' '  ./gradlew assembleDebug'