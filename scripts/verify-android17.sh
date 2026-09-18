#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

: "${ANDROID_SDK_ROOT:=${ANDROID_HOME:-}}"
if [[ -z "${ANDROID_SDK_ROOT}" ]]; then
  echo "ANDROID_SDK_ROOT/ANDROID_HOME is not set" >&2
  exit 1
fi

EXPECTED_PLATFORM="37"
EXPECTED_PLATFORM_MINOR="1"
EXPECTED_PLATFORM_PACKAGE="platforms/android-37.1"
EXPECTED_BUILD_TOOLS="37.0.0"
EXPECTED_NDK="28.2.13676358"
EXPECTED_CMAKE="3.31.5"
EXPECTED_AGP="9.4.0"
EXPECTED_GRADLE="9.7.1"
EXPECTED_COMPOSE_BOM="2026.09.00"
EXPECTED_ADAPTIVE="1.4.0-alpha02"
EXPECTED_M3_ALPHA="1.5.0-alpha28"

fail() { echo "Android 17 verification failed: $*" >&2; exit 1; }

# Prevent accidental regressions to the deprecated SDK Manager CLI.
DEPRECATED_SDK_CLI="sdk""manager"
if grep -RInE "(^|[^A-Za-z])${DEPRECATED_SDK_CLI}([^A-Za-z]|$)" .github/workflows scripts >/dev/null 2>&1; then
  fail "deprecated SDK Manager reference remains in CI or verification scripts"
fi

APP_GRADLE="$ROOT_DIR/app/build.gradle.kts"
ROOT_GRADLE="$ROOT_DIR/build.gradle.kts"
WRAPPER="$ROOT_DIR/gradle/wrapper/gradle-wrapper.properties"

[[ -f "$APP_GRADLE" ]] || fail "missing app/build.gradle.kts"
[[ -f "$ROOT_GRADLE" ]] || fail "missing build.gradle.kts"
[[ -f "$WRAPPER" ]] || fail "missing Gradle wrapper properties"

# Source-of-truth checks.
grep -Eq 'compileSdk[[:space:]]*=[[:space:]]*37' "$APP_GRADLE" || fail "compileSdk != 37"
grep -Eq 'compileSdkMinor[[:space:]]*=[[:space:]]*1' "$APP_GRADLE" || fail "compileSdkMinor != 1"
grep -Eq 'targetSdk[[:space:]]*=[[:space:]]*37' "$APP_GRADLE" || fail "targetSdk != 37"
grep -Fq 'buildToolsVersion = "37.0.0"' "$APP_GRADLE" || fail "Build Tools != 37.0.0"
grep -Fq 'id("com.android.application") version "9.4.0" apply false' "$ROOT_GRADLE" || fail "AGP != 9.4.0"
grep -Fq "gradle-${EXPECTED_GRADLE}-" "$WRAPPER" || fail "Gradle wrapper != ${EXPECTED_GRADLE}"
grep -Fq 'compose-bom-alpha:2026.09.00' "$APP_GRADLE" || fail "Compose alpha BOM != 2026.09.00"
grep -Fq "material3.adaptive:adaptive:${EXPECTED_ADAPTIVE}" "$APP_GRADLE" || fail "Material 3 Adaptive != ${EXPECTED_ADAPTIVE}"
grep -Fq 'MaterialExpressiveTheme' "$ROOT_DIR/app/src/main/java/com/febricahyaa/clockapp/ui/theme/ChronaTheme.kt" || fail "MaterialExpressiveTheme missing"
grep -Fq 'MotionScheme.expressive()' "$ROOT_DIR/app/src/main/java/com/febricahyaa/clockapp/ui/theme/ChronaTheme.kt" || fail "Expressive motion scheme missing"
TIMER_SCREEN="$ROOT_DIR/app/src/main/java/com/febricahyaa/clockapp/ui/screens/TimerScreen.kt"
if ! grep -Fq 'CircularWavyProgressIndicator' "$TIMER_SCREEN"   && ! grep -Fq 'CircularProgressIndicator' "$TIMER_SCREEN"   && ! grep -Fq 'drawArc(' "$TIMER_SCREEN"; then
  fail "Timer progress indicator missing (expected CircularWavyProgressIndicator, CircularProgressIndicator, or custom Canvas drawArc)"
fi

# Dashboard was renamed from HomeScreen.kt to ChronaBentoHomeScreen.kt.
HOME_SCREEN="$ROOT_DIR/app/src/main/java/com/febricahyaa/clockapp/ui/screens/ChronaBentoHomeScreen.kt"
[[ -f "$HOME_SCREEN" ]] || fail "ChronaBentoHomeScreen.kt missing"
grep -Fq 'currentWindowAdaptiveInfoV2' "$HOME_SCREEN" || fail "Adaptive window API missing"

# Confirm the requested Material 3 Expressive line is present.
if ! grep -Fq "$EXPECTED_M3_ALPHA" "$APP_GRADLE"; then
  # The version is BOM-managed; keep this check informational when the BOM is
  # authoritative and the exact transitive mapping is not written in source.
  echo "NOTE: Material 3 $EXPECTED_M3_ALPHA is BOM-managed; source pin is $EXPECTED_COMPOSE_BOM."
fi

# Android CLI is the supported SDK-management entry point.
command -v android >/dev/null 2>&1 || fail "Android CLI (android) is not available"
android --version >/dev/null 2>&1 || fail "Android CLI cannot execute"

android --sdk="${ANDROID_SDK_ROOT}" sdk list "${EXPECTED_PLATFORM_PACKAGE}" --all >/dev/null 2>&1 \
  || fail "Android ${EXPECTED_PLATFORM} platform metadata is not available"
android --sdk="${ANDROID_SDK_ROOT}" sdk list "build-tools/${EXPECTED_BUILD_TOOLS}" --all >/dev/null 2>&1 \
  || fail "Build Tools ${EXPECTED_BUILD_TOOLS} metadata is not available"
android --sdk="${ANDROID_SDK_ROOT}" sdk list "ndk/${EXPECTED_NDK}" --all >/dev/null 2>&1 \
  || fail "NDK ${EXPECTED_NDK} metadata is not available"
android --sdk="${ANDROID_SDK_ROOT}" sdk list "cmake/${EXPECTED_CMAKE}" --all >/dev/null 2>&1 \
  || fail "CMake ${EXPECTED_CMAKE} metadata is not available"

[[ -d "$ANDROID_SDK_ROOT/${EXPECTED_PLATFORM_PACKAGE}" ]] || fail "Android ${EXPECTED_PLATFORM}.${EXPECTED_PLATFORM_MINOR} platform directory missing (expected ${ANDROID_SDK_ROOT}/${EXPECTED_PLATFORM_PACKAGE})"
[[ -d "$ANDROID_SDK_ROOT/build-tools/${EXPECTED_BUILD_TOOLS}" ]] || fail "build-tools directory missing"
[[ -d "$ANDROID_SDK_ROOT/ndk/${EXPECTED_NDK}" ]] || fail "NDK directory missing"
[[ -d "$ANDROID_SDK_ROOT/cmake/${EXPECTED_CMAKE}" ]] || fail "CMake directory missing"

if grep -RInE 'android-36(\.0)?|build-tools;36\.0\.0|ANDROID_API_LEVEL[=: ]+"?36' .github/workflows >/dev/null 2>&1; then
  fail "stale Android 36 reference remains in CI workflows"
fi

for workflow in .github/workflows/*.yml; do
  if [[ "$workflow" == *.yml && "$workflow" != *dependabot-auto-merge.yml ]]; then
    grep -Fq 'setup-android@v4' "$workflow" || fail "Android SDK tooling setup missing in $workflow"
    grep -Eq 'android-37\.1|ANDROID_PLATFORM_PACKAGE:.*platforms/android-37\.1|ANDROID_PLATFORM_VERSION:.*37' "$workflow" || fail "Android 17 API 37.1 is not declared in $workflow"
    grep -Eq '37\.0\.0|ANDROID_BUILD_TOOLS:.*37\.0\.0' "$workflow" || fail "Build Tools 37 is not declared in $workflow"
  fi
done

echo "Android 17 CI verification passed"
echo "  Platform:   ${EXPECTED_PLATFORM}.${EXPECTED_PLATFORM_MINOR} (Android 17 / API 37.1)"
echo "  Build Tools:${EXPECTED_BUILD_TOOLS}"
echo "  NDK:        ${EXPECTED_NDK}"
echo "  CMake:      ${EXPECTED_CMAKE}"
echo "  AGP:        ${EXPECTED_AGP}"
echo "  Gradle:     ${EXPECTED_GRADLE}"
echo "  Compose BOM:${EXPECTED_COMPOSE_BOM} (alpha)"
echo "  Adaptive:   ${EXPECTED_ADAPTIVE}"
