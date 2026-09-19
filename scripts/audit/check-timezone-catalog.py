#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[2]
source = ROOT / "app/src/main/java/com/febricahyaa/clockapp/data/timezone/TimeZoneCatalog.kt"
text = source.read_text(encoding="utf-8")
required = [
    "IcuTimeZone.getTZDataVersion()",
    "ZoneId.getAvailableZoneIds()",
    "IcuTimeZone.getCanonicalID",
    "IcuTimeZone.getRegion",
]
missing = [item for item in required if item not in text]
if missing:
    raise SystemExit(f"Dynamic timezone catalog is missing required runtime API(s): {missing}")
if re.search(r"DEFAULT_.*ZONE_IDS\s*=\s*listOf", text):
    raise SystemExit("TimeZoneCatalog must not embed a frozen hand-maintained city database")
if text.count("data class Entry") != 1:
    raise SystemExit("Expected exactly one TimeZoneCatalog.Entry definition")

print("Dynamic timezone catalog: PASS")