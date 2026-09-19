#!/usr/bin/env python3
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

from __future__ import annotations

from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
CODE_EXTENSIONS = {".kt", ".java", ".cpp", ".h"}
HASH_EXTENSIONS = {".sh", ".py"}
GRADLE_EXTENSIONS = {".gradle", ".kts"}
SUPPORTED_EXTENSIONS = CODE_EXTENSIONS | HASH_EXTENSIONS | GRADLE_EXTENSIONS | {".xml"}

EXCLUDED_DIRS = {".git", ".gradle", "build", ".idea", "third_party", "archive"}


def should_check(path: Path) -> bool:
    if not path.is_file() or path.suffix.lower() not in SUPPORTED_EXTENSIONS:
        return False
    relative = path.relative_to(ROOT)
    if any(part in EXCLUDED_DIRS for part in relative.parts):
        return False
    return True


def has_expected_header(path: Path, text: str) -> bool:
    text = text.removeprefix("\ufeff").replace("\r\n", "\n").replace("\r", "\n")
    lines = text.splitlines()

    if path.suffix.lower() in CODE_EXTENSIONS:
        header = [
            "/*",
            " * Copyright 2026 Febrian Rahmad Cahya",
            " * SPDX-License-Identifier: MIT",
            " */",
        ]
        return lines[:4] == header

    if path.suffix.lower() in HASH_EXTENSIONS:
        index = 1 if lines and lines[0].startswith("#!") else 0
        header = [
            "# Copyright 2026 Febrian Rahmad Cahya",
            "# SPDX-License-Identifier: MIT",
        ]
        return lines[index:index + 2] == header

    if path.suffix.lower() in GRADLE_EXTENSIONS:
        header = [
            "/*",
            " * Copyright 2026 Febrian Rahmad Cahya",
            " * SPDX-License-Identifier: MIT",
            " */",
        ]
        return lines[:4] == header

    if path.suffix.lower() == ".xml":
        header = [
            "<!--",
            "  ~ Copyright 2026 Febrian Rahmad Cahya",
            "  ~ SPDX-License-Identifier: MIT",
            "  -->",
        ]
        return (
            bool(lines)
            and lines[0].lstrip().startswith("<?xml")
            and lines[1:5] == header
        )

    return False


failures = []
checked = 0

for path in sorted(ROOT.rglob("*")):
    if not should_check(path):
        continue
    checked += 1
    try:
        text = path.read_text(encoding="utf-8")
    except UnicodeDecodeError as exc:
        failures.append(f"{path.relative_to(ROOT)}: UTF-8 decode failed: {exc}")
        continue
    if not has_expected_header(path, text):
        failures.append(
            f"{path.relative_to(ROOT)}: missing or malformed canonical MIT copyright header"
        )

if failures:
    print("Copyright header audit: FAIL")
    for item in failures:
        print(f"  - {item}")
    raise SystemExit(1)

print(f"Copyright header audit: PASS ({checked} source files)")
