#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'EOF'
Usage: ./scripts/dev/verify.sh [quick|full|release]

quick   Static repository, resource, licensing, Material 3, workflow and Telegram checks.
full    quick + JVM unit tests + lint + debug APK.
release full non-signing validation for release candidates.
EOF
}

MODE="${1:-quick}"
case "$MODE" in
  quick)
    bash scripts/audit/source-audit.sh
    ;;
  full)
    bash scripts/audit/source-audit.sh
    ./gradlew --no-daemon --stacktrace testOssDebugUnitTest lintOssDebug assembleOssDebug
    ;;
  release)
    bash scripts/audit/source-audit.sh
    ./gradlew --no-daemon --stacktrace testOssDebugUnitTest lintOssDebug assembleOssDebug
    ;;
  -h|--help)
    usage
    ;;
  *)
    usage >&2
    exit 2
    ;;
esac

printf 'Chrona verification mode: %s — PASS\n' "$MODE"
