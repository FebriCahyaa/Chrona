#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REPO="$ROOT"

if [[ $# -gt 0 ]]; then
  REPO="$(cd "$1" && pwd)"
fi

install -D "$ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/components/WorldClockMap.kt" \
  "$REPO/app/src/main/java/com/febricahyaa/clockapp/ui/components/WorldClockMap.kt"
install -D "$ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/components/WorldClockMapData.kt" \
  "$REPO/app/src/main/java/com/febricahyaa/clockapp/ui/components/WorldClockMapData.kt"
install -D "$ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockScreen.kt" \
  "$REPO/app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockScreen.kt"

cd "$REPO"
python3 "$SCRIPT_DIR/update-world-clock-strings.py"

printf '%s\n' "World Clock revamp sources installed into: $REPO"
