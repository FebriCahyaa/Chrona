#!/usr/bin/env bash
set -euo pipefail
ROOT="${1:-.}"
cd "$ROOT"
TARGET="app/src/main/java/com/febricahyaa/clockapp/ui/screens/ChronaBentoHomeScreen.kt"
[[ -f "$TARGET" ]] || { echo "FAIL: target file missing"; exit 1; }
python3 - "$TARGET" <<'PY'
from pathlib import Path
import sys
p=Path(sys.argv[1])
s=p.read_text(encoding="utf-8")
checks={
"ClockModeSwitcher":"private fun ClockModeSwitcher(",
"ClockModeChoice":"private fun ClockModeChoice(",
"UTC offset helper":"private fun formatUtcOffset(",
"80sp hero typography":"fontSize = 80.sp",
"tabular numerals":"fontFeatureSettings = \"tnum\"",
"persisted display mode plumbing":"clockDisplayMode: ClockDisplayMode",
"smooth analog renderer":"rememberSmoothZonedNow",
}
missing=[name for name,needle in checks.items() if needle not in s]
if missing:
    print("FAIL: missing " + ", ".join(missing))
    raise SystemExit(1)
print("PASS: Phase 2 clock experience markers found")
PY
