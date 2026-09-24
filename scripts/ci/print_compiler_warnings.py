"""Print every Kotlin compiler diagnostic recorded in Gradle's problems report.

Gradle's console only lists the first handful of compiler problems; the rest live in
build/reports/problems/problems-report.html, whose data is an embedded JSON blob. This
prints all of them and emits GitHub annotations so they show up on the workflow run.
"""
from __future__ import annotations

import json
import re
import sys
from pathlib import Path

DATA_BLOCK = re.compile(r"//\s*begin-report-data\s*(.*?)\s*//\s*end-report-data", re.S)
REPO_PREFIX = re.compile(r"^.*?/(app/src/.*)$")


def load_report(html: str) -> object | None:
    match = DATA_BLOCK.search(html)
    if not match:
        return None
    return json.loads(match.group(1))


def _texts(value: object) -> list[str]:
    if isinstance(value, str):
        return [value]
    if isinstance(value, list):
        return [text for item in value for text in _texts(item)]
    if isinstance(value, dict):
        return _texts(value.get("text", []))
    return []


def iter_diagnostics(node: object):
    """Yields (path, line, message) for every entry that carries a source location."""
    if isinstance(node, dict):
        locations = node.get("locations")
        if isinstance(locations, list) and locations:
            message = " ".join(_texts(node.get("problemDetails")) or _texts(node.get("problem")))
            for location in locations:
                if isinstance(location, dict) and location.get("path"):
                    yield str(location["path"]), location.get("line"), message.strip()
        for child in node.values():
            yield from iter_diagnostics(child)
    elif isinstance(node, list):
        for child in node:
            yield from iter_diagnostics(child)


def relative(path: str) -> str:
    match = REPO_PREFIX.match(path)
    return match.group(1) if match else path


def main() -> int:
    if len(sys.argv) != 2:
        raise SystemExit("usage: print_compiler_warnings.py PROBLEMS_REPORT_HTML")
    report = Path(sys.argv[1])
    if not report.is_file():
        print(f"No problems report at {report}.")
        return 0
    data = load_report(report.read_text(encoding="utf-8", errors="replace"))
    if data is None:
        print("Problems report has no embedded data block; format may have changed.")
        return 0
    diagnostics = sorted(set(iter_diagnostics(data)), key=lambda d: (d[0], d[1] or 0, d[2]))
    print(f"{len(diagnostics)} compiler diagnostics:")
    for path, line, message in diagnostics:
        where = relative(path) + (f":{line}" if line else "")
        print(f"{where}: {message}")
        print(f"::warning file={relative(path)},line={line or 1}::{message}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
