#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

"""Reject Material 2 Compose UI imports while permitting Material icon packages."""
from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BAD_IMPORT = re.compile(r"^\s*import\s+androidx\.compose\.material\.(?!icons\.)")

errors: list[str] = []
for root in (
    ROOT / "app/src/main/java",
    ROOT / "app/src/test/java",
    ROOT / "app/src/androidTest/java",
):
    if not root.exists():
        continue
    for path in root.rglob("*.kt"):
        for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), 1):
            if BAD_IMPORT.search(line):
                errors.append(f"{path.relative_to(ROOT)}:{number}: {line.strip()}")

if errors:
    print("Material 2 Compose imports detected:")
    print("\n".join(errors))
    raise SystemExit(1)

print("Material 3 source audit: PASS")
