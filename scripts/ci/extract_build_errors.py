from __future__ import annotations

import re
import sys
from pathlib import Path

ERROR_PATTERNS = (
    re.compile(r"(?:^|\b)(?:e:|error:|fatal:|FAILURE:|Execution failed|Caused by:)", re.IGNORECASE),
    re.compile(r"(?:FAILED|FAILURE)\b"),
    re.compile(r"Could not resolve|Could not find|Unresolved reference|AAPT2|Lint found", re.IGNORECASE),
)

def extract(log_path: Path, limit: int = 120) -> str:
    lines = log_path.read_text(encoding="utf-8", errors="replace").splitlines()
    selected = [line for line in lines if any(pattern.search(line) for pattern in ERROR_PATTERNS)]
    if not selected:
        selected = lines[-80:]
    deduplicated = list(dict.fromkeys(selected))
    return "\n".join(deduplicated[-limit:]) + "\n"

def main() -> int:
    if len(sys.argv) != 3:
        raise SystemExit("usage: extract_build_errors.py LOG OUTPUT")
    source = Path(sys.argv[1])
    output = Path(sys.argv[2])
    output.write_text(extract(source), encoding="utf-8")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
