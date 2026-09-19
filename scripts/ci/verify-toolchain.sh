#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
set -euo pipefail

EXPECTED_GRADLE='9.7.1'
EXPECTED_COMPILE_PLATFORM='android-37.1'
EXPECTED_BUILD_TOOLS='37.0.0'
EXPECTED_NDK='28.2.13676358'
EXPECTED_CMAKE='3.31.5'

java -version
./gradlew --version | tee /tmp/chrona-gradle-version.txt

grep -Fq "gradle-${EXPECTED_GRADLE}-bin.zip" gradle/wrapper/gradle-wrapper.properties
command -v sdkmanager >/dev/null
a=$(sdkmanager --version)
printf 'sdkmanager: %s\\n' "$a"
command -v adb >/dev/null

if [[ "${CHRONA_REQUIRE_NATIVE_TOOLCHAIN:-1}" == "1" ]]; then
  command -v cmake >/dev/null
fi

test -d "${ANDROID_SDK_ROOT}/platforms/${EXPECTED_COMPILE_PLATFORM}"
test -d "${ANDROID_SDK_ROOT}/build-tools/${EXPECTED_BUILD_TOOLS}"
if [[ "${CHRONA_REQUIRE_NATIVE_TOOLCHAIN:-1}" == "1" ]]; then
  test -d "${ANDROID_SDK_ROOT}/ndk/${EXPECTED_NDK}"
  test -d "${ANDROID_SDK_ROOT}/cmake/${EXPECTED_CMAKE}"
fi

if [[ "${CHRONA_REQUIRE_ANDROID_CLI:-0}" == '1' ]]; then
  command -v android >/dev/null
  android --version
fi

echo 'Chrona toolchain: PASS'
