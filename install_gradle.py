#!/usr/bin/env python3

from __future__ import annotations

import hashlib
import os
import shutil
import stat
import subprocess
import sys
import tarfile
import tempfile
import urllib.request
import zipfile
from pathlib import Path


GRADLE_VERSION = "9.6.1"
GRADLE_FILE = f"gradle-{GRADLE_VERSION}-bin.zip"
GRADLE_URL = (
    f"https://services.gradle.org/distributions/{GRADLE_FILE}"
)

ROOT = Path(__file__).resolve().parent
LOCAL_DIR = ROOT / ".gradle-local"
DOWNLOAD_DIR = LOCAL_DIR / "downloads"
GRADLE_HOME = LOCAL_DIR / f"gradle-{GRADLE_VERSION}"
GRADLE_BIN = GRADLE_HOME / "bin" / "gradle"
GRADLE_WRAPPER = ROOT / "gradlew"


def print_line(message: str = "") -> None:
    print(message, flush=True)


def command_exists(command: str) -> bool:
    return shutil.which(command) is not None


def check_java() -> None:
    java_home = os.environ.get("JAVA_HOME")

    if java_home:
        java_bin = Path(java_home) / "bin" / "java"

        if java_bin.exists():
            print_line(f"[OK] JAVA_HOME: {java_home}")
            return

    if command_exists("java"):
        result = subprocess.run(
            ["java", "-version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
        )

        output = result.stdout.strip()
        print_line("[INFO] Java ditemukan:")
        print_line(output)

        if "version" not in output:
            raise RuntimeError("Java tidak dapat diverifikasi.")

        return

    raise RuntimeError(
        "Java tidak ditemukan. Pasang OpenJDK 17 terlebih dahulu:\n"
        "  apt update\n"
        "  apt install -y openjdk-17-jdk"
    )


def download_file(url: str, destination: Path) -> None:
    destination.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    if destination.exists() and destination.stat().st_size > 0:
        print_line(f"[OK] Arsip sudah tersedia: {destination}")
        return

    print_line(f"[DOWNLOAD] {url}")
    print_line(f"[TARGET]   {destination}")

    request = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Chrona-Gradle-Installer/1.0",
        },
    )

    with urllib.request.urlopen(request, timeout=120) as response:
        total = response.headers.get("Content-Length")
        total_size = int(total) if total else None
        downloaded = 0

        with destination.open("wb") as output:
            while True:
                chunk = response.read(1024 * 1024)

                if not chunk:
                    break

                output.write(chunk)
                downloaded += len(chunk)

                if total_size:
                    percent = downloaded * 100 // total_size
                    print(
                        f"\rProgress: {percent:3d}%",
                        end="",
                        flush=True,
                    )
                else:
                    print(
                        f"\rDownloaded: "
                        f"{downloaded / 1024 / 1024:.1f} MB",
                        end="",
                        flush=True,
                    )

    print_line()
    print_line("[OK] Download selesai.")


def extract_gradle(archive: Path) -> None:
    if GRADLE_BIN.exists():
        print_line(f"[OK] Gradle sudah diekstrak: {GRADLE_HOME}")
        return

    LOCAL_DIR.mkdir(
        parents=True,
        exist_ok=True,
    )

    temporary_dir = Path(
        tempfile.mkdtemp(
            prefix="chrona-gradle-",
            dir=str(LOCAL_DIR),
        )
    )

    try:
        print_line(f"[EXTRACT] {archive}")

        with zipfile.ZipFile(archive, "r") as zip_file:
            zip_file.extractall(temporary_dir)

        extracted_dir = temporary_dir / f"gradle-{GRADLE_VERSION}"

        if not extracted_dir.exists():
            raise RuntimeError(
                "Folder Gradle hasil ekstraksi tidak ditemukan."
            )

        if GRADLE_HOME.exists():
            shutil.rmtree(GRADLE_HOME)

        shutil.move(
            str(extracted_dir),
            str(GRADLE_HOME),
        )

        print_line(f"[OK] Gradle tersedia di: {GRADLE_HOME}")

    finally:
        shutil.rmtree(
            temporary_dir,
            ignore_errors=True,
        )


def write_gradlew() -> None:
    content = f'''#!/usr/bin/env sh

set -eu

APP_HOME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
GRADLE_HOME="$APP_HOME/.gradle-local/gradle-{GRADLE_VERSION}"
GRADLE_BIN="$GRADLE_HOME/bin/gradle"

if [ ! -x "$GRADLE_BIN" ]; then
    echo "Gradle {GRADLE_VERSION} belum tersedia."
    echo "Jalankan:"
    echo "  python3 install_gradle.py"
    exit 1
fi

if [ -z "${{JAVA_HOME:-}}" ]; then
    if command -v java >/dev/null 2>&1; then
        JAVA_BIN="$(command -v java)"
        JAVA_REAL="$(readlink -f "$JAVA_BIN" 2>/dev/null || true)"

        if [ -n "$JAVA_REAL" ]; then
            JAVA_HOME="$(dirname "$(dirname "$JAVA_REAL")")"
            export JAVA_HOME
        fi
    fi
fi

exec "$GRADLE_BIN" "$@"
'''

    GRADLE_WRAPPER.write_text(
        content,
        encoding="utf-8",
    )

    current_mode = GRADLE_WRAPPER.stat().st_mode

    GRADLE_WRAPPER.chmod(
        current_mode
        | stat.S_IRUSR
        | stat.S_IWUSR
        | stat.S_IXUSR
        | stat.S_IRGRP
        | stat.S_IXGRP
        | stat.S_IROTH
        | stat.S_IXOTH
    )

    print_line(f"[OK] Wrapper diperbaiki: {GRADLE_WRAPPER}")


def verify_gradle() -> None:
    print_line("\n[VERIFY] Gradle version")

    result = subprocess.run(
        [
            str(GRADLE_WRAPPER),
            "--version",
        ],
        cwd=ROOT,
        text=True,
    )

    if result.returncode != 0:
        raise RuntimeError(
            "Gradle gagal dijalankan."
        )


def run_build() -> None:
    print_line("\n[BUILD] Menjalankan build Chrona")

    tasks = [
        "testDebugUnitTest",
        "lintDebug",
        "assembleDebug",
        "--stacktrace",
        "--no-daemon",
    ]

    result = subprocess.run(
        [str(GRADLE_WRAPPER), *tasks],
        cwd=ROOT,
    )

    if result.returncode != 0:
        raise SystemExit(result.returncode)


def main() -> None:
    build_requested = "--build" in sys.argv

    print_line("=" * 60)
    print_line("Chrona Gradle 9.6.1 Installer")
    print_line("=" * 60)
    print_line(f"Root: {ROOT}")
    print_line(f"Gradle: {GRADLE_VERSION}")

    try:
        check_java()

        archive = DOWNLOAD_DIR / GRADLE_FILE

        download_file(
            GRADLE_URL,
            archive,
        )

        extract_gradle(archive)
        write_gradlew()
        verify_gradle()

        print_line("\n[SUCCESS] Gradle lokal berhasil dipasang.")

        if build_requested:
            run_build()
        else:
            print_line("\nJalankan build dengan:")
            print_line(
                "./gradlew "
                "testDebugUnitTest lintDebug assembleDebug "
                "--stacktrace --no-daemon"
            )

    except urllib.error.URLError as error:
        print_line(f"\n[ERROR] Gagal mengunduh Gradle: {error}")
        sys.exit(1)

    except zipfile.BadZipFile:
        print_line(
            "\n[ERROR] Arsip Gradle rusak atau bukan ZIP valid."
        )
        sys.exit(1)

    except RuntimeError as error:
        print_line(f"\n[ERROR] {error}")
        sys.exit(1)


if __name__ == "__main__":
    main()
