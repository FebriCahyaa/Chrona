#!/usr/bin/env python3
"""Audit Android string resources and source references without requiring Gradle."""
from __future__ import annotations
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / "app/src/main/res"
JAVA = ROOT / "app/src/main/java"
TEST = ROOT / "app/src/test"


def parse_strings(path: Path) -> dict[str, str]:
    root = ET.parse(path).getroot()
    out: dict[str, str] = {}
    for node in root.findall("string"):
        name = node.attrib.get("name")
        if name in out:
            raise AssertionError(f"duplicate string resource {name} in {path}")
        out[name] = "".join(node.itertext())
    return out


def main() -> int:
    default = parse_strings(RES / "values/strings.xml")
    refs: set[str] = set()
    text_sources: list[Path] = []
    for base in (JAVA, TEST):
        text_sources.extend(base.rglob("*.kt"))
        text_sources.extend(base.rglob("*.java"))
    text_sources.extend(ROOT.joinpath("app/src/main").rglob("*.xml"))
    for path in text_sources:
        text = path.read_text(encoding="utf-8", errors="ignore")
        refs.update(re.findall(r"R\.string\.([A-Za-z0-9_]+)", text))
        refs.update(re.findall(r"@string/([A-Za-z0-9_]+)", text))
    missing = sorted(refs - set(default))
    if missing:
        print("MISSING DEFAULT STRINGS:")
        for key in missing:
            print(f"  {key}")
        return 1

    for path in sorted(RES.glob("values-*/strings.xml")):
        localized = parse_strings(path)
        stale = sorted(set(localized) - set(default))
        if stale:
            print(f"STALE {path.relative_to(ROOT)}:")
            for key in stale:
                print(f"  {key}")
            return 1

    print(f"OK: {len(default)} default strings; {len(refs)} referenced resource keys")
    return 0


if __name__ == "__main__":
    sys.exit(main())
