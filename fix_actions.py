#!/usr/bin/env python3

from __future__ import annotations

import os
import re
import shutil
import stat
import sys
from datetime import datetime
from pathlib import Path


ROOT = Path.cwd()
WORKFLOW_DIR = ROOT / ".github" / "workflows"
GRADLEW = ROOT / "gradlew"

WORKFLOWS = {
    "ci.yml",
    "build.yml",
    "codeql.yml",
    "update-toolchain.yml",
}


def log(message: str) -> None:
    print(f"[Chrona-Actions] {message}")


def fail(message: str) -> None:
    print(f"[Chrona-Actions] ERROR: {message}", file=sys.stderr)
    sys.exit(1)


def backup_file(path: Path, backup_root: Path) -> None:
    if not path.exists():
        return

    destination = backup_root / path.relative_to(ROOT)
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(path, destination)
    log(f"Backup: {path.relative_to(ROOT)}")


def make_executable(path: Path) -> None:
    if not path.exists():
        fail(f"File tidak ditemukan: {path}")

    mode = path.stat().st_mode
    new_mode = mode | stat.S_IXUSR | stat.S_IXGRP | stat.S_IXOTH

    if mode != new_mode:
        path.chmod(new_mode)
        log(f"chmod +x: {path.relative_to(ROOT)}")
    else:
        log(f"Permission sudah executable: {path.relative_to(ROOT)}")


def replace_all(text: str, replacements: list[tuple[str, str]]) -> str:
    result = text

    for old, new in replacements:
        result = result.replace(old, new)

    return result


def add_gradlew_permission_step(text: str) -> str:
    marker = "      - name: Fix Gradle wrapper permissions\n"

    if marker in text:
        return text

    checkout_patterns = [
        "      - name: Checkout\n        uses: actions/checkout@v7\n",
        "      - name: Checkout source\n        uses: actions/checkout@v7\n",
        "      - name: Checkout\n        uses: actions/checkout@v4\n",
        "      - name: Checkout source\n        uses: actions/checkout@v4\n",
    ]

    step = (
        "      - name: Fix Gradle wrapper permissions\n"
        "        shell: bash\n"
        "        run: |\n"
        "          set -euo pipefail\n"
        "          chmod +x ./gradlew\n"
        "          test -x ./gradlew\n"
        "          ls -l ./gradlew\n"
    )

    for pattern in checkout_patterns:
        if pattern in text:
            return text.replace(pattern, pattern + "\n" + step, 1)

    # Fallback: insert before first step if checkout format differs.
    match = re.search(r"(?m)^      - name: ", text)
    if match:
        return text[:match.start()] + step + "\n" + text[match.start():]

    return text


def fix_ci_yml(text: str) -> str:
    text = add_gradlew_permission_step(text)

    # API 37 / Android 17 toolchain
    text = replace_all(
        text,
        [
            (
                "name: Install Android 17 SDK 37 preview toolchain",
                "name: Install Android 17 SDK 37 toolchain",
            ),
            (
                "'platforms;android-37.0'",
                "'platforms;android-37.0'",
            ),
            (
                "'build-tools;37.0.0'",
                "'build-tools;37.0.0'",
            ),
        ],
    )

    # Make wrapper executable before every actual wrapper use.
    text = text.replace(
        "      - name: Unit tests\n"
        "        run: ./gradlew --no-daemon testDebugUnitTest --stacktrace",
        "      - name: Unit tests\n"
        "        run: |\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --no-daemon testDebugUnitTest --stacktrace",
    )

    text = text.replace(
        "      - name: Android lint\n"
        "        run: ./gradlew --no-daemon lintDebug --stacktrace",
        "      - name: Android lint\n"
        "        run: |\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --no-daemon lintDebug --stacktrace",
    )

    text = text.replace(
        "      - name: Assemble debug\n"
        "        run: ./gradlew --no-daemon assembleDebug --stacktrace",
        "      - name: Assemble debug\n"
        "        run: |\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --no-daemon assembleDebug --stacktrace",
    )

    text = text.replace(
        "      - name: Assemble release\n"
        "        run: ./gradlew --no-daemon assembleRelease --stacktrace",
        "      - name: Assemble release\n"
        "        run: |\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --no-daemon assembleRelease --stacktrace",
    )

    return text


def fix_build_yml(text: str) -> str:
    text = add_gradlew_permission_step(text)

    # Project compileSdk is 37, therefore build workflow must install API 37.
    text = text.replace(
        "- name: Install Android SDK 36",
        "- name: Install Android SDK 37",
    )

    text = text.replace(
        '"platforms;android-36"',
        '"platforms;android-37.0"',
    )

    # Build-tools 36.0.0 is valid with this project and can remain.
    # Add NDK + CMake because app/build.gradle.kts explicitly requires them.
    old_block = (
        "          sdkmanager --install \\\n"
        '            "platform-tools" \\\n'
        '            "platforms;android-37.0" \\\n'
        '            "build-tools;36.0.0"'
    )

    new_block = (
        "          yes | sdkmanager --licenses >/dev/null || true\n"
        "          sdkmanager --install --channel=1 \\\n"
        '            "platform-tools" \\\n'
        '            "platforms;android-37.0" \\\n'
        '            "build-tools;36.0.0" \\\n'
        '            "cmake;3.31.6" \\\n'
        '            "ndk;30.0.16248370"'
    )

    if old_block in text:
        text = text.replace(old_block, new_block, 1)

    # Replace plain gradle with project wrapper.
    text = text.replace(
        "gradle --no-daemon --stacktrace --build-cache assembleDebug",
        "./gradlew --no-daemon --stacktrace --build-cache assembleDebug",
    )

    return text


def fix_codeql_yml(text: str) -> str:
    text = add_gradlew_permission_step(text)

    # Keep the existing API 37 preview toolchain.
    text = text.replace(
        "Install Android 17 SDK 37 preview toolchain",
        "Install Android 17 SDK 37 toolchain",
    )

    # Ensure CodeQL build explicitly fixes executable permission.
    old = (
        "      - name: Build for CodeQL extraction\n"
        "        run: |\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --no-daemon assembleDebug --stacktrace"
    )

    new = (
        "      - name: Build for CodeQL extraction\n"
        "        run: |\n"
        "          set -euo pipefail\n"
        "          chmod +x ./gradlew\n"
        "          test -x ./gradlew\n"
        "          ./gradlew --no-daemon assembleDebug --stacktrace"
    )

    text = text.replace(old, new)

    return text


def fix_update_toolchain_yml(text: str) -> str:
    # Remove deprecated/problematic packages input completely.
    text = re.sub(
        r"\n\s*with:\n\s*packages:.*?(?=\n\s*-\s+name:|\n\s*uses:|\Z)",
        "\n",
        text,
        flags=re.DOTALL,
    )

    # Explicit SDK setup remains manual.
    text = text.replace(
        "uses: android-actions/setup-android@v3",
        "uses: android-actions/setup-android@v4",
    )

    # API 37 project
    text = text.replace(
        '"platforms;android-37"',
        '"platforms;android-37.0"',
    )

    # More robust licenses handling.
    text = text.replace(
        "yes | sdkmanager --licenses >/dev/null || true",
        "yes | sdkmanager --licenses >/dev/null 2>&1 || true",
    )

    # Ensure Gradle wrapper is executable before validation.
    old = (
        "      - name: Validate Gradle wrapper\n"
        "        shell: bash\n"
        "        run: |\n"
        "          set -euo pipefail\n"
        "\n"
        "          chmod +x ./gradlew\n"
        "          ./gradlew --version"
    )

    new = (
        "      - name: Validate Gradle wrapper\n"
        "        shell: bash\n"
        "        run: |\n"
        "          set -euo pipefail\n"
        "\n"
        "          chmod +x ./gradlew\n"
        "          test -x ./gradlew\n"
        "          ./gradlew --version"
    )

    text = text.replace(old, new)

    return text


def sanitize_packages_input(text: str) -> str:
    """
    Remove dangerous/invalid setup-android package values such as:
      packages: 'tools'
    Keep only useful package declarations.
    """
    lines = text.splitlines()
    output: list[str] = []

    skip_packages = False

    for line in lines:
        stripped = line.strip()

        if stripped.startswith("packages:"):
            value = stripped.split(":", 1)[1].strip().strip("'\"")

            if value.lower() in {"tools", "tool", "android-sdk-tools"}:
                log("Menghapus packages: tools")
                continue

        output.append(line)

    return "\n".join(output) + ("\n" if text.endswith("\n") else "")


def write_workflow(path: Path, original: str, updated: str) -> bool:
    updated = sanitize_packages_input(updated)

    if updated == original:
        log(f"Tidak ada perubahan: {path.relative_to(ROOT)}")
        return False

    path.write_text(updated, encoding="utf-8")
    log(f"Diperbaiki: {path.relative_to(ROOT)}")
    return True


def validate_workflow(path: Path) -> list[str]:
    errors: list[str] = []

    if not path.exists():
        errors.append("file workflow tidak ditemukan")
        return errors

    text = path.read_text(encoding="utf-8")

    # No invalid setup-android package value.
    if re.search(r"(?im)^\s*packages:\s*['\"]?tools['\"]?\s*$", text):
        errors.append("masih terdapat packages: tools")

    # All workflows using Gradle wrapper should contain chmod.
    if "./gradlew" in text and "chmod +x ./gradlew" not in text:
        errors.append("workflow memakai ./gradlew tetapi tidak ada chmod +x ./gradlew")

    # Check basic required workflow structure.
    if not re.search(r"(?m)^name:\s+", text):
        errors.append("name workflow tidak ditemukan")

    if not re.search(r"(?m)^on:\s*$", text):
        errors.append("trigger 'on:' tidak ditemukan")

    if not re.search(r"(?m)^jobs:\s*$", text):
        errors.append("jobs: tidak ditemukan")

    return errors


def verify_gradle_wrapper() -> None:
    if not GRADLEW.exists():
        fail("gradlew tidak ditemukan di root repository.")

    make_executable(GRADLEW)

    # Android shared storage biasanya tidak menerapkan execute bit.
    # Git tetap dapat menyimpan mode executable menggunakan update-index.
    if str(GRADLEW).startswith("/storage/"):
        log("Android shared storage terdeteksi.")
        log("Execute bit tidak dapat diverifikasi melalui os.access().")
        log("Gunakan git update-index --chmod=+x gradlew.")
        return

    if not os.access(GRADLEW, os.X_OK):
        fail("gradlew masih belum executable.")

    log("gradlew executable: OK")


def main() -> None:
    log(f"Repository root: {ROOT}")

    if not WORKFLOW_DIR.exists():
        fail(
            "Direktori .github/workflows tidak ditemukan. "
            "Jalankan script dari root repository Chrona."
        )

    if not (ROOT / "settings.gradle.kts").exists():
        fail("settings.gradle.kts tidak ditemukan. Pastikan script dijalankan dari root repo.")

    # Verify Gradle wrapper first.
    verify_gradle_wrapper()

    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    backup_root = WORKFLOW_DIR / f".python-fix-backup-{timestamp}"

    changed_files: list[str] = []

    for workflow_name in WORKFLOWS:
        path = WORKFLOW_DIR / workflow_name

        if not path.exists():
            log(f"Skip, tidak ditemukan: {path.relative_to(ROOT)}")
            continue

        original = path.read_text(encoding="utf-8")
        backup_file(path, backup_root)

        if workflow_name == "ci.yml":
            updated = fix_ci_yml(original)
        elif workflow_name == "build.yml":
            updated = fix_build_yml(original)
        elif workflow_name == "codeql.yml":
            updated = fix_codeql_yml(original)
        elif workflow_name == "update-toolchain.yml":
            updated = fix_update_toolchain_yml(original)
        else:
            updated = original

        if write_workflow(path, original, updated):
            changed_files.append(str(path.relative_to(ROOT)))

    print()
    log("Validasi workflow...")

    validation_failed = False

    for workflow_name in WORKFLOWS:
        path = WORKFLOW_DIR / workflow_name

        if not path.exists():
            continue

        errors = validate_workflow(path)

        if errors:
            validation_failed = True
            print(f"\n  [FAIL] {path.relative_to(ROOT)}")
            for error in errors:
                print(f"         - {error}")
        else:
            print(f"  [OK]   {path.relative_to(ROOT)}")

    print()
    print("=" * 70)

    if validation_failed:
        print("HASIL: MASIH ADA MASALAH PADA WORKFLOW")
        print(f"Backup tersedia di: {backup_root.relative_to(ROOT)}")
        sys.exit(2)

    print("HASIL: ACTIONS BERHASIL DIPERBAIKI")
    print()

    if changed_files:
        print("File yang berubah:")
        for item in changed_files:
            print(f"  - {item}")
    else:
        print("Tidak ada isi workflow yang berubah.")

    print()
    print("Gradle wrapper:")
    print("  - ./gradlew executable : OK")
    print()
    print(f"Backup:")
    print(f"  - {backup_root.relative_to(ROOT)}")
    print()
    print("Selanjutnya:")
    print("  git status")
    print("  git diff -- .github/workflows")
    print("  git add .github/workflows gradlew")
    print('  git commit -m "fix(ci): repair Android GitHub Actions toolchain"')
    print("  git push origin main")
    print("=" * 70)


if __name__ == "__main__":
    main()
