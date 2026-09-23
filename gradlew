#!/usr/bin/env bash
set -euo pipefail
root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
gradle_version="9.7.1"
gradle_bin="${GRADLE_BIN:-}"
if [[ -n "$gradle_bin" ]]; then
    exec "$gradle_bin" "$@"
fi
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
fi
cache_dir="${GRADLE_USER_HOME:-$HOME/.gradle}/chrona-wrapper/$gradle_version"
distribution="$cache_dir/gradle-$gradle_version"
zip="$cache_dir/gradle-$gradle_version-bin.zip"
mkdir -p "$cache_dir"
if [[ ! -x "$distribution/bin/gradle" ]]; then
    command -v curl >/dev/null 2>&1 || { printf 'Gradle is not installed and curl is unavailable.\n' >&2; exit 1; }
    if [[ ! -f "$zip" ]]; then
        curl --fail --location --retry 3 --output "$zip" "https://services.gradle.org/distributions/gradle-$gradle_version-bin.zip"
    fi
    rm -rf "$distribution"
    unzip -q "$zip" -d "$cache_dir"
fi
exec "$distribution/bin/gradle" "$@"
