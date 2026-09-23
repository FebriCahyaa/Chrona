from __future__ import annotations

import re
import sys
from pathlib import Path

REQUIRED_OUTPUTS = ("arm64-v8a", "armeabi-v7a", "x86", "x86_64", "universal")


def find_apks(directory: Path, variant: str) -> dict[str, Path]:
    pattern = re.compile(rf".*-(?P<output>arm64-v8a|armeabi-v7a|x86_64|x86|universal)-{re.escape(variant)}\.apk$")
    results: dict[str, Path] = {}
    for path in sorted(directory.glob("*.apk")):
        match = pattern.match(path.name)
        if match:
            results[match.group("output")] = path
    return results


def main() -> int:
    if len(sys.argv) != 3:
        raise SystemExit("usage: verify_abi_outputs.py APK_DIRECTORY VARIANT")
    directory = Path(sys.argv[1])
    variant = sys.argv[2]
    if not directory.is_dir():
        raise SystemExit(f"APK directory does not exist: {directory}")
    found = find_apks(directory, variant)
    missing = [output for output in REQUIRED_OUTPUTS if output not in found]
    if missing:
        raise SystemExit(f"Missing {variant} APK outputs: {', '.join(missing)}")
    for output in REQUIRED_OUTPUTS:
        print(f"{output}: {found[output]}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
