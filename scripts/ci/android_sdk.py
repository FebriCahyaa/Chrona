"""Resolve, install and verify the Android SDK packages for one SDK channel.

Each Chrona build line compiles against a different Android SDK channel:

    0 stable  -> Release (stable branch, v* tags)
    1 beta    -> Debug   (main branch, pull requests)
    2 dev     -> Dev     (dev branch)
    3 canary  -> Canary  (canary branch)

A channel includes every package from the more stable channels, so the newest
platform and build-tools listed for a channel is what that build line uses.
Versions are discovered from `sdkmanager --list --channel=N` rather than
hard-coded, so a package that does not exist on a channel can no longer break
CI. Explicit versions can still be pinned with --platform / --build-tools.

Results are exported for Gradle (see app/build.gradle.kts):

    CHRONA_COMPILE_SDK   "37.2", "37" or a preview codename
    CHRONA_BUILD_TOOLS   e.g. "37.0.0" or "37.1.0-rc1"
"""

from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path

CHANNEL_NAMES = {0: "stable", 1: "beta", 2: "dev", 3: "canary"}

RELEASE_PLATFORM = re.compile(r"^platforms;android-(\d+)(?:\.(\d+))?$")
PREVIEW_PLATFORM = re.compile(r"^platforms;android-([A-Za-z][A-Za-z0-9]*)$")
BUILD_TOOLS = re.compile(r"^build-tools;(\d+)\.(\d+)\.(\d+)(?:[- ]?rc(\d+))?$")


@dataclass(frozen=True)
class Selection:
    platform: str  # sdkmanager path, e.g. "platforms;android-37.2"
    compile_sdk: str  # Gradle value, e.g. "37.2" or "CinnamonBun"
    build_tools: str  # sdkmanager version, e.g. "37.0.0"


def fail(message: str) -> None:
    print(f"::error::{message}", file=sys.stderr)
    raise SystemExit(1)


def parse_available(listing: str) -> list[str]:
    """Return package paths from the 'Available Packages' table of sdkmanager --list."""
    paths: list[str] = []
    in_available = False
    for raw in listing.replace("\r", "\n").splitlines():
        line = raw.strip()
        if line.startswith("Available Packages"):
            in_available = True
            continue
        if line.startswith(("Installed packages", "Available Updates")):
            in_available = False
            continue
        if not in_available or "|" not in line:
            continue
        path = line.split("|", 1)[0].strip()
        if path and path != "Path" and not path.startswith("-"):
            paths.append(path)
    return paths


def choose_platform(paths: list[str], channel: int) -> tuple[str, str]:
    releases: list[tuple[tuple[int, int], str, str]] = []
    previews: list[tuple[str, str]] = []
    for path in paths:
        if match := RELEASE_PLATFORM.match(path):
            major, minor = int(match[1]), int(match[2] or 0)
            compile_sdk = f"{major}.{match[2]}" if match[2] else str(major)
            releases.append(((major, minor), path, compile_sdk))
        elif match := PREVIEW_PLATFORM.match(path):
            previews.append((path, match[1]))
    # Preview (codename) platforms only ship on pre-release channels and are
    # always newer than the latest numbered release.
    if channel > 0 and previews:
        path, codename = sorted(previews)[-1]
        return path, codename
    if not releases:
        fail(f"No Android platform found on SDK channel {channel}")
    _, path, compile_sdk = max(releases)
    return path, compile_sdk


def choose_build_tools(paths: list[str]) -> str:
    versions: list[tuple[tuple[int, int, int, float], str]] = []
    for path in paths:
        if match := BUILD_TOOLS.match(path):
            rc = float(match[4]) if match[4] else float("inf")
            key = (int(match[1]), int(match[2]), int(match[3]), rc)
            versions.append((key, path.split(";", 1)[1]))
    if not versions:
        fail("No Android build-tools found on the selected SDK channel")
    return max(versions)[1]


def sdkmanager(sdk_root: Path) -> str:
    for candidate in (
        shutil.which("sdkmanager"),
        sdk_root / "cmdline-tools/latest/bin/sdkmanager",
    ):
        if candidate and Path(candidate).is_file():
            return str(candidate)
    fail("sdkmanager was not found; run android-actions/setup-android first")
    return ""


def run(command: list[str], stdin: str | None = None) -> str:
    print("+", " ".join(command), flush=True)
    result = subprocess.run(command, input=stdin, capture_output=True, text=True)
    if result.returncode != 0:
        print(result.stdout, result.stderr, sep="\n", file=sys.stderr)
        fail(f"Command failed with exit code {result.returncode}: {' '.join(command)}")
    return result.stdout


def verify(sdk_root: Path, selection: Selection) -> None:
    platform_dir = sdk_root / "platforms" / selection.platform.split(";", 1)[1]
    if not (platform_dir / "android.jar").is_file():
        fail(f"Missing Android platform: {platform_dir / 'android.jar'}")
    build_tools_dir = sdk_root / "build-tools" / selection.build_tools
    for tool in ("aapt2", "apksigner", "zipalign"):
        if not (build_tools_dir / tool).is_file():
            fail(f"Missing build-tools binary: {build_tools_dir / tool}")


def export(selection: Selection, channel: int) -> None:
    values = {
        "CHRONA_SDK_CHANNEL": str(channel),
        "CHRONA_SDK_CHANNEL_NAME": CHANNEL_NAMES[channel],
        "CHRONA_COMPILE_SDK": selection.compile_sdk,
        "CHRONA_BUILD_TOOLS": selection.build_tools,
    }
    for variable in ("GITHUB_ENV", "GITHUB_OUTPUT"):
        target = os.environ.get(variable)
        if not target:
            continue
        with open(target, "a", encoding="utf-8") as handle:
            for key, value in values.items():
                name = key if variable == "GITHUB_ENV" else key.lower().replace("_", "-")
                handle.write(f"{name}={value}\n")
    summary = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary:
        with open(summary, "a", encoding="utf-8") as handle:
            handle.write(
                "### Android SDK\n\n"
                "| Channel | Platform | compileSdk | Build Tools |\n"
                "| --- | --- | --- | --- |\n"
                f"| {channel} ({CHANNEL_NAMES[channel]}) | `{selection.platform}` "
                f"| `{selection.compile_sdk}` | `{selection.build_tools}` |\n\n"
            )


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__.split("\n", 1)[0])
    parser.add_argument("--channel", type=int, choices=sorted(CHANNEL_NAMES), required=True)
    parser.add_argument("--platform", default="", help="pin e.g. android-37.2")
    parser.add_argument("--build-tools", default="", help="pin e.g. 37.0.0")
    args = parser.parse_args()

    sdk_root = Path(os.environ.get("ANDROID_SDK_ROOT") or os.environ.get("ANDROID_HOME") or "")
    if not str(sdk_root) or not sdk_root.is_dir():
        fail("ANDROID_SDK_ROOT/ANDROID_HOME is not set to an existing directory")
    tool = sdkmanager(sdk_root)
    channel_flag = f"--channel={args.channel}"

    listing = run([tool, "--list", channel_flag, f"--sdk_root={sdk_root}"])
    available = parse_available(listing)
    if args.platform:
        platform = f"platforms;{args.platform.removeprefix('platforms;')}"
        if platform not in available:
            fail(f"{platform} is not available on SDK channel {args.channel}")
        platform, compile_sdk = choose_platform([platform], max(args.channel, 1))
    else:
        platform, compile_sdk = choose_platform(available, args.channel)
    build_tools = args.build_tools or choose_build_tools(available)
    if f"build-tools;{build_tools}" not in available:
        fail(f"build-tools;{build_tools} is not available on SDK channel {args.channel}")
    selection = Selection(platform, compile_sdk, build_tools)
    print(
        f"SDK channel {args.channel} ({CHANNEL_NAMES[args.channel]}): "
        f"{selection.platform}, build-tools;{selection.build_tools}"
    )

    run([tool, "--licenses", channel_flag, f"--sdk_root={sdk_root}"], stdin="y\n" * 64)
    run(
        [
            tool,
            "--install",
            channel_flag,
            f"--sdk_root={sdk_root}",
            "platform-tools",
            selection.platform,
            f"build-tools;{selection.build_tools}",
        ],
        stdin="y\n" * 64,
    )
    verify(sdk_root, selection)
    export(selection, args.channel)
    print("Android SDK verification PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
