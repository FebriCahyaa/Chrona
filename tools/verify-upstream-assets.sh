#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

required=(
  "$ROOT_DIR/third_party/material-design-icons/README.md"
  "$ROOT_DIR/third_party/aosp/README.md"
  "$ROOT_DIR/third_party/aosp/SDK_PACKAGES.txt"
  "$ROOT_DIR/third_party/licenses/THIRD_PARTY_NOTICES.md"
)

for file in "${required[@]}"; do
  if [[ ! -s "$file" ]]; then
    echo "Missing required asset metadata: $file" >&2
    exit 1
  fi
done

if ! grep -Eqi 'Apache(-| )License ?2(\.0)?|Apache-2\.0' \
  "$ROOT_DIR/third_party/licenses/THIRD_PARTY_NOTICES.md"; then
  echo "Apache 2.0 notice was not found." >&2
  exit 1
fi

grep -q 'Android API: 37' \
  "$ROOT_DIR/third_party/aosp/SDK_PACKAGES.txt"

echo "Upstream asset metadata validation passed."
