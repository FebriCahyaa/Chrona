#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
UPSTREAM_DIR="${RUNNER_TEMP:-${ROOT_DIR}/.cache}/material-design-icons"
DEST_DIR="${ROOT_DIR}/third_party/material-design-icons"
RES_DIR="${ROOT_DIR}/app/src/main/res"
REVISION="${MATERIAL_ICONS_REVISION:-master}"

mkdir -p "$(dirname "$UPSTREAM_DIR")" "$DEST_DIR/metadata" "$RES_DIR/drawable" "$RES_DIR/font"

if [[ ! -d "$UPSTREAM_DIR/.git" ]]; then
  git clone --filter=blob:none --no-checkout --depth 1 \
    --branch "$REVISION" \
    https://github.com/google/material-design-icons.git "$UPSTREAM_DIR"
else
  git -C "$UPSTREAM_DIR" fetch --depth 1 origin "$REVISION"
  git -C "$UPSTREAM_DIR" checkout "$REVISION"
fi

UPSTREAM_COMMIT="$(git -C "$UPSTREAM_DIR" rev-parse HEAD)"
printf '%s\n' "$UPSTREAM_COMMIT" > "$DEST_DIR/metadata/UPSTREAM_COMMIT"
printf '%s\n' "https://github.com/google/material-design-icons" > "$DEST_DIR/metadata/UPSTREAM_URL"
printf '%s\n' "Apache-2.0" > "$DEST_DIR/metadata/LICENSE"

# Detect icon names used by Compose source. This is intentionally conservative.
mapfile -t ICONS < <(
  grep -RhoE 'Icons\.(Filled|Outlined|Rounded|Sharp|TwoTone|AutoMirrored\.[A-Za-z]+)\.[A-Za-z0-9_]+' \
    "$ROOT_DIR/app/src/main/java" "$ROOT_DIR/core" 2>/dev/null \
    | sed -E 's/^Icons\.//' | sort -u || true
)

printf '%s\n' "# Chrona icon usage manifest" > "$DEST_DIR/metadata/ICON_USAGE.txt"
printf '%s\n' "# Upstream commit: $UPSTREAM_COMMIT" >> "$DEST_DIR/metadata/ICON_USAGE.txt"
printf '%s\n' "${ICONS[@]:-none}" >> "$DEST_DIR/metadata/ICON_USAGE.txt"

# Copy only upstream documentation/license metadata; selected vector assets are generated
# by the optional generator when the matching upstream SVG is available.
if [[ -f "$UPSTREAM_DIR/LICENSE" ]]; then
  cp "$UPSTREAM_DIR/LICENSE" "$DEST_DIR/metadata/MATERIAL_DESIGN_ICONS_LICENSE"
fi

cat > "$DEST_DIR/metadata/SOURCE_INFO.txt" <<INFO
Source: https://github.com/google/material-design-icons
Revision: $UPSTREAM_COMMIT
License: Apache-2.0
Selection: Icons referenced by Chrona Compose source
INFO

echo "Material assets synchronized at $UPSTREAM_COMMIT"
