#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
fail=0

if grep -Fq 'implementation("androidx.compose.runtime:runtime-saveable")' "$ROOT/app/build.gradle.kts"; then
  echo 'PASS: runtime-saveable dependency declared'
else
  echo 'FAIL: runtime-saveable dependency missing'; fail=1
fi

if grep -Fq 'import androidx.compose.runtime.rememberSaveable' "$ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/screens/AlarmScreen.kt"; then
  echo 'PASS: AlarmScreen rememberSaveable import'
else
  echo 'FAIL: AlarmScreen rememberSaveable import missing'; fail=1
fi

if grep -Fq 'import androidx.compose.foundation.layout.weight' "$ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/screens/OnboardingScreen.kt"; then
  echo 'FAIL: invalid OnboardingScreen weight import'; fail=1
else
  echo 'PASS: no invalid OnboardingScreen weight import'
fi

[[ -f "$ROOT/app/src/main/java/com/febricahyaa/clockapp/core/AlarmTriggerPolicy.kt" ]] \
  && echo 'PASS: AlarmTriggerPolicy source exists' \
  || { echo 'FAIL: AlarmTriggerPolicy source missing'; fail=1; }

clock_count=$(find "$ROOT/app/src/main/java" -type f \( -name 'ClockTimeMath.kt' -o -name 'ClockTimeMath.java' \) -print | wc -l)
widget_count=$(find "$ROOT/app/src/main/java" -type f \( -name 'ChronaClockWidgetProvider.kt' -o -name 'ChronaClockWidgetProvider.java' \) -print | wc -l)
[[ "$clock_count" -eq 1 ]] && echo 'PASS: single ClockTimeMath source' || { echo "FAIL: ClockTimeMath source count=$clock_count"; fail=1; }
[[ "$widget_count" -eq 1 ]] && echo 'PASS: single ChronaClockWidgetProvider source' || { echo "FAIL: ChronaClockWidgetProvider source count=$widget_count"; fail=1; }

if grep -RIn --include='*.kt' --include='*.java' 'import androidx.compose.foundation.layout.weight' "$ROOT/app/src/main/java"; then
  echo 'FAIL: invalid direct weight imports remain'; fail=1
else
  echo 'PASS: no invalid direct weight imports remain'
fi

if [[ "$fail" -ne 0 ]]; then exit 1; fi
