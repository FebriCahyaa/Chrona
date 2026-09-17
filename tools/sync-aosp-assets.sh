#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
OUT_DIR="$ROOT_DIR/third_party/aosp"
mkdir -p "$OUT_DIR"

cat > "$OUT_DIR/SDK_PACKAGES.txt" <<INFO
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
Android API: 37
Android SDK Platform: 37; compile extension/minor requirement is handled by the Android Gradle Plugin
Build Tools: 37.0.0
Java: 17
Android Gradle Plugin: 9.4.0
Gradle: 9.6.1
INFO

cat > "$OUT_DIR/INTEGRATION_SCOPE.md" <<INFO
<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Integration scope

Chrona uses the installed Android SDK and resolved AndroidX/Compose artifacts.

The complete AOSP tree is not copied into the application repository. Only source-level
patterns compatible with the current Android API, AndroidX, Compose, and Material 3 APIs
should be integrated into Chrona.
INFO

echo "AOSP/Android integration metadata refreshed."
