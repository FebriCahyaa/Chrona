#!/usr/bin/env python3
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
from __future__ import annotations

from pathlib import Path

NOTICE = "Copyright (c) 2026 Febrian Rahmad Cahya"
ROOT = Path(__file__).resolve().parents[2]
TEXT_EXTENSIONS = {
    ".kt", ".java", ".cpp", ".h", ".kts", ".pro", ".properties",
    ".xml", ".yml", ".yaml", ".sh", ".py", ".md", ".toml", ".txt",
}
EXCLUDED_DIRS = {".git", ".gradle", "build", "app/build", ".idea", "docs/archive"}
EXCLUDED_NAMES = {"gradlew", "gradlew.bat", "gradle-wrapper.properties"}


def should_check(path: Path) -> bool:
    if path.name in EXCLUDED_NAMES or path.suffix.lower() not in TEXT_EXTENSIONS:
        return False
    relative = path.relative_to(ROOT)
    if relative.parts[:2] == ("docs", "archive"):
        return False
    return not any(part in EXCLUDED_DIRS for part in relative.parts)


failures: list[str] = []
checked = 0
for path in ROOT.rglob("*"):
    if not path.is_file() or not should_check(path):
        continue
    checked += 1
    text = path.read_text(encoding="utf-8", errors="replace")
    if NOTICE not in text:
        failures.append(str(path.relative_to(ROOT)))

if failures:
    print("Missing project copyright notices:")
    print("\n".join(f"  - {item}" for item in failures))
    raise SystemExit(1)

print(f"Copyright coverage: PASS ({checked} active text/config files; historical docs/archive excluded)")
