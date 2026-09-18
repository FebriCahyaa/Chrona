#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

BUNDLE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
TARGET_ROOT="${1:-$(pwd)}"
TARGET_ROOT="$(cd "$TARGET_ROOT" && pwd)"

required=(
  "app/src/main/java/com/febricahyaa/clockapp/ui/screens/ChronaBentoHomeScreen.kt"
  "app/src/main/java/com/febricahyaa/clockapp/ui/screens/TimerScreen.kt"
  "app/src/main/java/com/febricahyaa/clockapp/ui/screens/WorldClockSearchScreen.kt"
  "scripts/verify-android17.sh"
)
for rel in "${required[@]}"; do
  [[ -f "$BUNDLE_ROOT/$rel" ]] || { echo "Missing bundle file: $rel" >&2; exit 1; }
done

for rel in "${required[@]}"; do
  mkdir -p "$TARGET_ROOT/$(dirname "$rel")"
  cp -f "$BUNDLE_ROOT/$rel" "$TARGET_ROOT/$rel"
done

rm -f "$TARGET_ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/screens/HomeScreen.kt"
rm -f "$TARGET_ROOT/app/src/main/java/com/febricahyaa/clockapp/ui/components/ChronaAnimatedSlider.kt"

chmod +x "$TARGET_ROOT/scripts/verify-android17.sh" 2>/dev/null || true

printf '%s\n' "Chrona 0.10.2 compile repair applied to: $TARGET_ROOT"
printf '%s\n' "Removed: HomeScreen.kt, ChronaAnimatedSlider.kt"
printf '%s\n' "Replaced: ChronaBentoHomeScreen.kt, TimerScreen.kt, WorldClockSearchScreen.kt, verify-android17.sh"
printf '%s\n' "Next: cd \"$TARGET_ROOT\" && git status --short && ./gradlew --no-daemon --stacktrace test"
