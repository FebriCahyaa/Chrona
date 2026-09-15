#!/usr/bin/env python3
"""Repair Chrona Android source for AGP 9.4 / built-in Kotlin.

Designed for Ubuntu/Ubuntu Proot. It only edits source/build files; it does not
modify .github. A timestamped backup is created before any write.
"""
from __future__ import annotations

import argparse
import datetime as dt
import re
import shutil
import subprocess
import sys
from pathlib import Path

AGP = "9.4.0"
GRADLE = "9.6.1"
KOTLIN = "2.3.21"
NDK = "28.2.13676358"
CMAKE = "3.31.6"
COMPILE_SDK = 37
TARGET_SDK = 37
COMPOSE_BOM = "2026.08.00"
JAVA = 17


def git_root() -> Path:
    try:
        p = subprocess.run(
            ["git", "rev-parse", "--show-toplevel"],
            capture_output=True,
            text=True,
            check=True,
        )
        return Path(p.stdout.strip()).resolve()
    except Exception:
        p = Path.cwd().resolve()
        if (p / ".git").exists():
            return p
        raise SystemExit("[ERROR] Jalankan script dari root repository Git.")


def backup_files(root: Path, files: list[Path]) -> Path:
    stamp = dt.datetime.now().strftime("%Y%m%d-%H%M%S")
    backup = root / f".chrona-source-backup-{stamp}"
    backup.mkdir(parents=True, exist_ok=False)
    for rel in files:
        src = root / rel
        if src.exists():
            dst = backup / rel
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)
    return backup


def write_if_changed(path: Path, new_text: str) -> bool:
    old = path.read_text(encoding="utf-8")
    if old == new_text:
        print(f"[SKIP] {path.relative_to(path.parents[3] if len(path.parents) >= 3 else path.parent)}")
        return False
    path.write_text(new_text, encoding="utf-8")
    print(f"[OK]   {path}")
    return True


def patch_root_gradle(root: Path) -> None:
    path = root / "build.gradle.kts"
    text = path.read_text(encoding="utf-8")

    # Remove the AGP-incompatible/obsolete application of Kotlin Android plugin.
    text = re.sub(
        rf'^\s*id\("org\.jetbrains\.kotlin\.android"\)\s+version\s+"{re.escape(KOTLIN)}"\s+apply\s+false\s*\n',
        "",
        text,
        flags=re.MULTILINE,
    )

    # Keep Compose Compiler plugin on the same Kotlin line, while letting AGP provide
    # built-in Kotlin support and explicitly providing the newer KGP implementation.
    if "org.jetbrains.kotlin.plugin.compose" not in text:
        text = text.rstrip() + f'\n    id("org.jetbrains.kotlin.plugin.compose") version "{KOTLIN}" apply false\n}}\n'

    if "buildscript {" not in text:
        text = f'''buildscript {{
    dependencies {{
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:{KOTLIN}")
    }}
}}

{text.lstrip()}'''
    elif "org.jetbrains.kotlin:kotlin-gradle-plugin" not in text:
        text = re.sub(
            r'(buildscript\s*\{\s*dependencies\s*\{)',
            rf'\1\n        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:{KOTLIN}")',
            text,
            count=1,
        )

    # Ensure there is exactly one application and one Compose plugin declaration.
    text = re.sub(
        rf'\n\s*id\("org\.jetbrains\.kotlin\.plugin\.compose"\)\s+version\s+"{re.escape(KOTLIN)}"\s+apply\s+false\s*\n\s*\}}\s*$',
        f'\n    id("org.jetbrains.kotlin.plugin.compose") version "{KOTLIN}" apply false\n}}\n',
        text,
        flags=re.MULTILINE,
    )

    path.write_text(text, encoding="utf-8")
    print(f"[OK]   {path}")


def patch_app_gradle(root: Path) -> None:
    path = root / "app/build.gradle.kts"
    text = path.read_text(encoding="utf-8")

    # AGP 9 built-in Kotlin: do not apply kotlin-android.
    text = re.sub(
        r'^\s*id\("org\.jetbrains\.kotlin\.android"\)\s*\n',
        "",
        text,
        flags=re.MULTILINE,
    )

    # Match AGP 9.4 default NDK to avoid requiring a newer unprovisioned NDK.
    text = re.sub(
        r'(^\s*ndkVersion\s*=\s*")[^"]+("\s*$)',
        rf'\g<1>{NDK}\g<2>',
        text,
        flags=re.MULTILINE,
    )

    # Keep the modern toolchain values and current Compose BOM.
    text = re.sub(r'compileSdk\s*=\s*\d+', f'compileSdk = {COMPILE_SDK}', text)
    text = re.sub(r'targetSdk\s*=\s*\d+', f'targetSdk = {TARGET_SDK}', text)
    text = re.sub(r'jvmToolchain\(\d+\)', f'jvmToolchain({JAVA})', text)
    text = re.sub(r'JvmTarget\.JVM_\d+', f'JvmTarget.JVM_{JAVA}', text)
    text = re.sub(r'JavaVersion\.VERSION_\d+', f'JavaVersion.VERSION_{JAVA}', text)
    text = re.sub(r'androidx\.compose:compose-bom:[0-9.]+', f'androidx.compose:compose-bom:{COMPOSE_BOM}', text)

    path.write_text(text, encoding="utf-8")
    print(f"[OK]   {path}")


def patch_gradle_properties(root: Path) -> None:
    path = root / "gradle.properties"
    lines = path.read_text(encoding="utf-8").splitlines()
    out = []
    for line in lines:
        # Removed/deprecated in the AGP 9 path; the source should not carry the old opt-out.
        if line.strip().startswith("android.nonFinalResIds="):
            continue
        out.append(line)

    # Make the intended AGP 9 migration explicit.
    if not any(line.startswith("android.builtInKotlin=") for line in out):
        out.append("android.builtInKotlin=true")

    path.write_text("\n".join(out).rstrip() + "\n", encoding="utf-8")
    print(f"[OK]   {path}")


def patch_wrapper_properties(root: Path) -> None:
    path = root / "gradle/wrapper/gradle-wrapper.properties"
    text = path.read_text(encoding="utf-8")
    text = re.sub(
        r'distributionUrl=.*',
        f'distributionUrl=https\\://services.gradle.org/distributions/gradle-{GRADLE}-bin.zip',
        text,
    )
    path.write_text(text.rstrip() + "\n", encoding="utf-8")
    print(f"[OK]   {path}")


def patch_gradlew(root: Path) -> None:
    path = root / "gradlew"
    text = '''#!/usr/bin/env sh
set -eu

# Chrona local/CI-compatible Gradle launcher.
# Prefer a provisioned Gradle binary. The Gradle wrapper metadata is kept at
# gradle/wrapper/gradle-wrapper.properties for IDE/CI tooling.
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi

printf '%s\\n' "Gradle is not installed on PATH." >&2
printf '%s\\n' "Install Gradle 9.6.1 (or provision it in CI), then rerun ./gradlew." >&2
exit 127
'''
    path.write_text(text, encoding="utf-8")
    path.chmod(path.stat().st_mode | 0o111)
    print(f"[OK]   {path}")


def static_validate(root: Path) -> int:
    failures = []
    app = (root / "app/build.gradle.kts").read_text(encoding="utf-8")
    top = (root / "build.gradle.kts").read_text(encoding="utf-8")
    props = (root / "gradle.properties").read_text(encoding="utf-8")
    wrapper = (root / "gradle/wrapper/gradle-wrapper.properties").read_text(encoding="utf-8")

    checks = {
        "module does not apply kotlin-android": 'id("org.jetbrains.kotlin.android")' not in app,
        "top-level does not declare kotlin-android plugin": 'id("org.jetbrains.kotlin.android")' not in top,
        "Compose Compiler plugin is present": 'id("org.jetbrains.kotlin.plugin.compose")' in top and 'id("org.jetbrains.kotlin.plugin.compose")' in app,
        "KGP 2.3.21 is explicitly available": f'org.jetbrains.kotlin:kotlin-gradle-plugin:{KOTLIN}' in top,
        "built-in Kotlin explicitly enabled": "android.builtInKotlin=true" in props,
        "deprecated nonFinalResIds removed": "android.nonFinalResIds=" not in props,
        "Gradle 9.6.1 wrapper metadata": f"gradle-{GRADLE}-bin.zip" in wrapper,
        "API 37": f"compileSdk = {COMPILE_SDK}" in app and f"targetSdk = {TARGET_SDK}" in app,
        "current Compose BOM": f"androidx.compose:compose-bom:{COMPOSE_BOM}" in app,
        f"NDK {NDK}": f'ndkVersion = "{NDK}"' in app,
    }

    print("\nValidation:")
    for name, ok in checks.items():
        print(f"  {'PASS' if ok else 'FAIL'}  {name}")
        if not ok:
            failures.append(name)

    # Local-resource sanity checks.
    java_root = root / "app/src/main/java"
    source_text = "\n".join(p.read_text(encoding="utf-8", errors="ignore") for p in java_root.rglob("*") if p.is_file() and p.suffix in {".kt", ".java"})
    xml_text = "\n".join(p.read_text(encoding="utf-8", errors="ignore") for p in (root / "app/src/main/res").rglob("*.xml"))
    refs = set(re.findall(r'R\.string\.([A-Za-z0-9_]+)', source_text))
    defs = set(re.findall(r'<string\s+name="([^"]+)"', xml_text))
    missing = refs - defs
    print(f"  {'PASS' if not missing else 'FAIL'}  Resource string references ({len(refs)} refs)")
    if missing:
        print("       missing:", ", ".join(sorted(missing)))
        failures.append("missing string resources")

    return 1 if failures else 0


def run_build(root: Path) -> int:
    gradle = shutil.which("gradle")
    if not gradle:
        print("\n[INFO] Gradle tidak ditemukan di PATH; build dilewati.")
        print("       Di Ubuntu Proot, pasang/provision Gradle 9.6.1 lalu jalankan:")
        print("       ./gradlew testDebugUnitTest lintDebug assembleDebug")
        return 0

    commands = [
        [gradle, "--version"],
        [gradle, "testDebugUnitTest", "--stacktrace", "--no-daemon"],
        [gradle, "lintDebug", "--stacktrace", "--no-daemon"],
        [gradle, "assembleDebug", "--stacktrace", "--no-daemon"],
    ]

    for cmd in commands:
        print("\n$", " ".join(cmd))
        result = subprocess.run(cmd, cwd=root)
        if result.returncode != 0:
            print(f"[ERROR] Build step failed: {result.returncode}")
            return result.returncode
    return 0


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--build", action="store_true", help="Run unit test, lint, and debug build after patching")
    args = parser.parse_args()

    root = git_root()
    print(f"Chrona source repair\nRoot: {root}\nTarget: AGP {AGP}, Gradle {GRADLE}, Kotlin {KOTLIN}, JDK {JAVA}, API {COMPILE_SDK}")

    files = [
        Path("build.gradle.kts"),
        Path("app/build.gradle.kts"),
        Path("gradle.properties"),
        Path("gradle/wrapper/gradle-wrapper.properties"),
        Path("gradlew"),
    ]
    backup = backup_files(root, files)
    print(f"\n[BACKUP] {backup}")

    patch_root_gradle(root)
    patch_app_gradle(root)
    patch_gradle_properties(root)
    patch_wrapper_properties(root)
    patch_gradlew(root)

    status = static_validate(root)
    if status:
        print("\n[ERROR] Static validation failed. Backup tersedia di:", backup)
        return status

    if args.build:
        return run_build(root)

    print("\n[SUCCESS] Source repair selesai.")
    print("Untuk verifikasi build:")
    print("  ./gradlew testDebugUnitTest lintDebug assembleDebug --stacktrace --no-daemon")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
