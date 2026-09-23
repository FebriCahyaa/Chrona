from __future__ import annotations

import re
import subprocess
import sys
from collections import defaultdict
from typing import DefaultDict, List, Tuple

PREFIXES = {
    "feat": "Added",
    "fix": "Fixed",
    "perf": "Performance",
    "refactor": "Changed",
    "build": "Build",
    "ci": "CI",
    "docs": "Documentation",
    "test": "Tests",
    "chore": "Maintenance",
    "style": "Style",
}

COMMIT_PATTERN = re.compile(r"^(?P<type>[a-zA-Z]+)(?:\([^)]*\))?!?:\s*(?P<message>.+)$")

def run_git(*args: str) -> str:
    result = subprocess.run(["git", *args], check=True, text=True, capture_output=True)
    return result.stdout.strip()

def previous_tag(current: str) -> str | None:
    tags = run_git("tag", "--sort=-version:refname").splitlines()
    for tag in tags:
        if tag != current:
            return tag
    return None

def collect_range(previous: str | None, current: str) -> List[Tuple[str, str]]:
    revision = f"{previous}..{current}" if previous else current
    lines = run_git("log", revision, "--format=%s").splitlines()
    grouped: List[Tuple[str, str]] = []
    for line in lines:
        match = COMMIT_PATTERN.match(line)
        if match:
            grouped.append((PREFIXES.get(match.group("type").lower(), "Other"), match.group("message")))
        elif line:
            grouped.append(("Other", line))
    return grouped

def render(current: str, previous: str | None, entries: List[Tuple[str, str]]) -> str:
    grouped: DefaultDict[str, List[str]] = defaultdict(list)
    for section, message in entries:
        grouped[section].append(message)
    lines = [f"# Chrona {current}", ""]
    if previous:
        lines.append(f"Changes since {previous}.")
        lines.append("")
    order = ["Added", "Fixed", "Performance", "Changed", "Build", "CI", "Documentation", "Tests", "Maintenance", "Style", "Other"]
    for section in order:
        values = grouped.get(section)
        if values:
            lines.append(f"## {section}")
            lines.extend(f"- {value}" for value in values)
            lines.append("")
    return "\n".join(lines).rstrip() + "\n"

def main() -> int:
    current = sys.argv[1] if len(sys.argv) > 1 else run_git("describe", "--tags", "--exact-match")
    previous = previous_tag(current)
    content = render(current, previous, collect_range(previous, current))
    sys.stdout.write(content)
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
