#!/usr/bin/env bash
# Copyright 2026 Febrian Rahmad Cahya
# SPDX-License-Identifier: MIT

set -euo pipefail
ROOT_DIR="$(cd "$(dirname "$0")/../.." && pwd)"
cd "$ROOT_DIR"

failures=0

check_command() {
  local command_name="$1"
  if command -v "$command_name" >/dev/null 2>&1; then
    printf '✅ %-24s %s\n' "$command_name" "$(command -v "$command_name")"
  else
    printf '❌ %-24s missing\n' "$command_name"
    failures=$((failures + 1))
  fi
}

check_command python3
check_command grep
check_command find

if [[ -f app/src/main/cpp/chrona_time.cpp && -f app/src/main/cpp/chrona_clock.cpp ]]; then
  if command -v clang++ >/dev/null 2>&1; then
    JAVA_INCLUDE=""
    if [[ -n "${JAVA_HOME:-}" && -d "${JAVA_HOME}/include" ]]; then
      JAVA_INCLUDE="${JAVA_HOME}/include"
    elif command -v javac >/dev/null 2>&1; then
      JAVAC_REAL="$(readlink -f "$(command -v javac)")"
      JAVA_HOME_DETECTED="$(cd "$(dirname "$JAVAC_REAL")/.." && pwd)"
      JAVA_INCLUDE="${JAVA_HOME_DETECTED}/include"
    fi

    if [[ -d "$JAVA_INCLUDE" ]]; then
      clang++ -std=c++20 -Wall -Wextra -Werror=return-type -fsyntax-only \
        -I"$JAVA_INCLUDE" -I"$JAVA_INCLUDE/linux" \
        app/src/main/cpp/chrona_time.cpp \
        app/src/main/cpp/chrona_clock.cpp
      echo '✅ C++ JNI syntax          PASS'
    else
      echo 'ℹ️ C++ JNI syntax          SKIPPED (JNI headers unavailable)'
    fi

    AAudioInclude=""
    NDK_CLANG=""
    if [[ -n "${ANDROID_NDK_ROOT:-}" ]]; then
      candidate="${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64/bin/clang++"
      candidate_sysroot="${ANDROID_NDK_ROOT}/toolchains/llvm/prebuilt/linux-x86_64/sysroot"
      if [[ -x "$candidate" && -f "$candidate_sysroot/usr/include/aaudio/AAudio.h" ]]; then
        NDK_CLANG="$candidate"
        AAudioInclude="$candidate_sysroot"
      fi
    fi
    if [[ -n "$NDK_CLANG" ]]; then
      "$NDK_CLANG" \
        --target=aarch64-linux-android26 \
        --sysroot="$AAudioInclude" \
        -std=c++20 -Wall -Wextra -Werror=return-type -fsyntax-only \
        app/src/main/cpp/chrona_audio.cpp
      echo '✅ AAudio native syntax     PASS'
    else
      echo 'ℹ️ AAudio native syntax     SKIPPED (Android NDK AAudio toolchain unavailable)'
    fi
    fi
    if [[ -n "$AAudioInclude" ]]; then
      clang++ -std=c++20 -Wall -Wextra -Werror=return-type -fsyntax-only \
        -I"$AAudioInclude" \
        app/src/main/cpp/chrona_audio.cpp
      echo '✅ AAudio native syntax     PASS'
    else
      echo 'ℹ️ AAudio native syntax     SKIPPED (Android NDK AAudio headers unavailable)'
    fi
  else
    echo 'ℹ️ C++ syntax              SKIPPED (clang++ unavailable)'
fi
else
  echo 'ℹ️ C++ syntax              SKIPPED (native source unavailable)'
fi

python3 scripts/audit/check-copyright.py
python3 scripts/audit/resource-audit.py
python3 scripts/audit/license-audit.py
python3 scripts/localization/audit-resources.py
python3 scripts/audit/check-timezone-catalog.py
python3 scripts/audit/material3-only.py
python3 scripts/audit/enterprise-architecture-audit.py
python3 scripts/audit/workflow-audit.py
python3 -m unittest scripts/telegram/test_notify.py

if (( failures > 0 )); then
  echo "Repository audit failed with ${failures} missing host tools." >&2
  exit 1
fi

echo 'Repository audit: PASS'
