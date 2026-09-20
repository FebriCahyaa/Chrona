#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

"""Validate Chrona's intentional five-workflow CI surface."""
from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
WORKFLOWS = ROOT / ".github/workflows"
EXPECTED = {
    "debug-build.yml",
    "pull-request-issue.yml",
    "release-build.yml",
    "sync-source.yml",
    "update-commit.yml",
}

actual = {path.name for path in WORKFLOWS.glob("*.yml")}
missing = EXPECTED - actual
extra = actual - EXPECTED

if missing or extra:
    if missing:
        print("Missing workflows:", ", ".join(sorted(missing)))
    if extra:
        print("Unexpected workflows:", ", ".join(sorted(extra)))
    raise SystemExit(1)

print("Workflow contract audit: PASS (exactly five workflows)")
