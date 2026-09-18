#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

legacy_files=(
  "$ROOT/app/src/main/java/com/febricahyaa/clockapp/core/ClockTimeMath.java"
  "$ROOT/app/src/main/java/com/febricahyaa/clockapp/widget/ChronaClockWidgetProvider.java"
)

for file in "${legacy_files[@]}"; do
  if [[ -f "$file" ]]; then
    echo "ERROR: legacy duplicate source exists: ${file#$ROOT/}" >&2
    exit 1
  fi
done

echo "OK: no legacy ClockTimeMath/ChronaClockWidgetProvider Java duplicates found"
