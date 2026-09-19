#!/usr/bin/env python3
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
from __future__ import annotations

import argparse
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
DEFAULT = ROOT / "app/src/main/res/values/strings.xml"


def keys(path: Path) -> set[str]:
    return set(re.findall(r'<string\s+name="([^"]+)"', path.read_text(encoding="utf-8")))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--strict", action="store_true")
    args = parser.parse_args()

    source = keys(DEFAULT)
    failures = 0
    print(f"Source strings: {len(source)}")
    for path in sorted((DEFAULT.parent.parent).glob("values-*")):
        string_file = path / "strings.xml"
        if not string_file.exists():
            continue
        translated = keys(string_file)
        missing = sorted(source - translated)
        stale = sorted(translated - source)
        state = "PASS" if not stale and (not args.strict or not missing) else "WARN"
        print(f"{state:4} {path.name}: {len(translated)} keys; missing={len(missing)} stale={len(stale)}")
        if stale:
            failures += 1
            print("      stale:", ", ".join(stale[:10]))
        if args.strict and missing:
            failures += 1
            print("      missing:", ", ".join(missing[:10]))

    if failures:
        return 1
    print("Localization resource audit: PASS")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
