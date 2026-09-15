#!/usr/bin/env sh
set -eu

# Lightweight repository bootstrap. CI installs Gradle explicitly; local machines can use the
# installed Gradle binary or Android Studio. Keeping this script small avoids embedding binaries.
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
printf '%s\n' "Gradle 9.6.0 is required. Install Gradle or use Android Studio's bundled Gradle, then rerun." >&2
exit 127
