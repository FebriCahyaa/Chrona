#!/usr/bin/env bash
set -euo pipefail
ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

required=(
  "$ROOT_DIR/third_party/material-design-icons/README.md"
  "$ROOT_DIR/third_party/aosp/README.md"
  "$ROOT_DIR/third_party/licenses/THIRD_PARTY_NOTICES.md"
)
for file in "${required[@]}"; do
  test -s "$file" || { echo "Missing required asset metadata: $file" >&2; exit 1; }
done

grep -q 'Apache-2.0' "$ROOT_DIR/third_party/licenses/THIRD_PARTY_NOTICES.md"
grep -q 'Android API: 37' "$ROOT_DIR/third_party/aosp/SDK_PACKAGES.txt" 2>/dev/null || true

echo "Upstream asset metadata validation passed."
