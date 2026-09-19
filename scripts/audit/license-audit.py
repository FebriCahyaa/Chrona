#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

"""Validate that every declared dependency has an explicit license record."""
from __future__ import annotations

import re
import tomllib
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CATALOG = ROOT / "gradle/libs.versions.toml"
LICENSES = ROOT / "config/dependency-licenses.toml"

def module_of(value: str) -> str:
    match = re.search(r'module\s*=\s*"([^"]+)"', value)
    if not match:
        raise ValueError(f"Unable to parse dependency module from: {value}")
    return match.group(1)

catalog = tomllib.loads(CATALOG.read_text(encoding="utf-8"))
license_catalog = tomllib.loads(LICENSES.read_text(encoding="utf-8"))
entries = license_catalog.get("licenses", {})
missing: list[str] = []

for alias, value in catalog.get("libraries", {}).items():
    if not isinstance(value, dict) or "module" not in value:
        continue
    module = value["module"]
    if module not in entries:
        missing.append(f"{alias} -> {module}")

if missing:
    print("Dependencies without explicit license records:")
    print("\n".join(f"  - {item}" for item in missing))
    raise SystemExit(1)

for module, record in entries.items():
    if not record.get("license"):
        raise SystemExit(f"License record has no license name: {module}")
    if not record.get("upstream"):
        raise SystemExit(f"License record has no upstream URL: {module}")

print(f"License catalog: PASS ({len(entries)} declared/pinned artifacts documented)")