#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'EOF'
Usage: ./scripts/dev/verify.sh [quick|full|release]

quick   Static repository audits, Telegram renderer tests, and JVM unit tests.
full    quick + lint + debug APK + Android instrumentation when a device is available.
release full validation without signing; signing is performed only by release CI.
EOF
}

MODE="${1:-quick}"
case "$MODE" in
  quick)
    bash scripts/audit/source-audit.sh
    python3 -m unittest scripts/telegram/test_notify.py
    ./gradlew --no-daemon --stacktrace testDebugUnitTest
    ;;
  full)
    bash scripts/audit/source-audit.sh
    python3 -m unittest scripts/telegram/test_notify.py
    ./gradlew --no-daemon --stacktrace testDebugUnitTest lintDebug assembleDebug
    ./gradlew --no-daemon --stacktrace connectedDebugAndroidTest
    ;;
  release)
    bash scripts/audit/source-audit.sh
    python3 -m unittest scripts/telegram/test_notify.py
    ./gradlew --no-daemon --stacktrace testDebugUnitTest lintDebug
    ;;
  -h|--help) usage ;;
  *) usage >&2; exit 2 ;;
esac

printf 'Chrona verification mode: %s — PASS\n' "$MODE"