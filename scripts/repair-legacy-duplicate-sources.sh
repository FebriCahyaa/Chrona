#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

KOTLIN_CLOCK="$ROOT/app/src/main/java/com/febricahyaa/clockapp/core/ClockTimeMath.kt"
JAVA_CLOCK="$ROOT/app/src/main/java/com/febricahyaa/clockapp/core/ClockTimeMath.java"
KOTLIN_WIDGET="$ROOT/app/src/main/java/com/febricahyaa/clockapp/widget/ChronaClockWidgetProvider.kt"
JAVA_WIDGET="$ROOT/app/src/main/java/com/febricahyaa/clockapp/widget/ChronaClockWidgetProvider.java"

removed=0

if [[ -f "$JAVA_CLOCK" ]]; then
  [[ -f "$KOTLIN_CLOCK" ]] || { echo "ERROR: refusing to delete ClockTimeMath.java because Kotlin replacement is missing" >&2; exit 1; }
  rm -f "$JAVA_CLOCK"
  echo "Removed duplicate: ${JAVA_CLOCK#$ROOT/}"
  removed=1
fi

if [[ -f "$JAVA_WIDGET" ]]; then
  [[ -f "$KOTLIN_WIDGET" ]] || { echo "ERROR: refusing to delete ChronaClockWidgetProvider.java because Kotlin replacement is missing" >&2; exit 1; }
  rm -f "$JAVA_WIDGET"
  echo "Removed duplicate: ${JAVA_WIDGET#$ROOT/}"
  removed=1
fi

if [[ "$removed" -eq 0 ]]; then
  echo "No legacy duplicate Java sources found."
fi
