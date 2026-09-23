from __future__ import annotations

import os
import sys
from pathlib import Path

EXPECTED_PLATFORM = "android-37.2"
EXPECTED_PLATFORM_REVISION = "1"
EXPECTED_BUILD_TOOLS = "37.1.0"


def read_properties(path: Path) -> dict[str, str]:
    values: dict[str, str] = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        if "=" in line and not line.lstrip().startswith("#"):
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip()
    return values


def fail(message: str) -> None:
    print(f"[ERROR] {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> int:
    sdk_root = Path(os.environ.get("ANDROID_SDK_ROOT", os.environ.get("ANDROID_HOME", "")))
    if not sdk_root:
        fail("ANDROID_SDK_ROOT/ANDROID_HOME is not set")

    platform_dir = sdk_root / "platforms" / EXPECTED_PLATFORM
    platform_props = platform_dir / "source.properties"
    if not (platform_dir / "android.jar").is_file():
        fail(f"Missing Android platform: {platform_dir / 'android.jar'}")
    if not platform_props.is_file():
        fail(f"Missing platform metadata: {platform_props}")
    props = read_properties(platform_props)
    if props.get("Pkg.Path") != f"platforms;{EXPECTED_PLATFORM}":
        fail(f"Unexpected platform package path: {props.get('Pkg.Path')!r}")
    if props.get("Pkg.Revision") != EXPECTED_PLATFORM_REVISION:
        fail(f"Unexpected {EXPECTED_PLATFORM} revision: {props.get('Pkg.Revision')!r}")

    build_tools_dir = sdk_root / "build-tools" / EXPECTED_BUILD_TOOLS
    build_tools_props = build_tools_dir / "source.properties"
    if not (build_tools_dir / "aapt2").is_file() or not (build_tools_dir / "apksigner").is_file():
        fail(f"Missing Android Build Tools binaries under {build_tools_dir}")
    if not build_tools_props.is_file():
        fail(f"Missing build-tools metadata: {build_tools_props}")
    build_props = read_properties(build_tools_props)
    if build_props.get("Pkg.Path") != f"build-tools;{EXPECTED_BUILD_TOOLS}":
        fail(f"Unexpected build-tools package path: {build_props.get('Pkg.Path')!r}")
    if build_props.get("Pkg.Revision") != EXPECTED_BUILD_TOOLS:
        fail(f"Unexpected build-tools revision: {build_props.get('Pkg.Revision')!r}")

    print(f"Android SDK verification PASS: {EXPECTED_PLATFORM} rev {EXPECTED_PLATFORM_REVISION}, Build Tools {EXPECTED_BUILD_TOOLS}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
