#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail

TAG="${1:?release tag is required}"
VERSION="${TAG#v}"
[[ "$TAG" =~ ^v[0-9]+\.[0-9]+\.[0-9]+([.-][0-9A-Za-z.-]+)?$ ]]

CURRENT_VERSION="$(sed -nE 's/^[[:space:]]*versionName = "([^"]+)".*/\1/p' app/build.gradle.kts | head -n 1)"
[[ "$CURRENT_VERSION" == "$VERSION" ]] || {
  echo "Release tag $TAG does not match source versionName $CURRENT_VERSION" >&2
  exit 1
}

./gradlew --no-daemon --stacktrace testOssDebugUnitTest lintOssDebug