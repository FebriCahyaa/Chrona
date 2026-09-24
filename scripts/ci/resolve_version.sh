#!/usr/bin/env bash
# Resolve versionCode/versionName for a build line and export them for Gradle.
#
#   usage: resolve_version.sh VARIANT
#
# versionCode = commit count of HEAD (monotonic along each branch).
# versionName = latest vX.Y.Z tag; non-release lines append the short SHA.
# Needs a full-history checkout (actions/checkout fetch-depth: 0).
set -euo pipefail

variant="${1:?usage: resolve_version.sh VARIANT}"
code="$(git rev-list --count HEAD)"
tag="$(git describe --tags --abbrev=0 --match 'v[0-9]*.[0-9]*.[0-9]*' 2>/dev/null || echo v1.0.0)"
name="${tag#v}"
if [[ "$variant" != release ]]; then
    name="${name}-$(git rev-parse --short=8 HEAD)"
fi
task="${variant^}"

{
    printf 'CHRONA_VERSION_CODE=%s\n' "$code"
    printf 'CHRONA_VERSION_NAME=%s\n' "$name"
    printf 'VARIANT_TASK=%s\n' "$task"
} >> "${GITHUB_ENV:-/dev/null}"
{
    printf 'code=%s\n' "$code"
    printf 'name=%s\n' "$name"
    printf 'artifact=chrona-%s-apk-%s\n' "$variant" "${GITHUB_RUN_ID:-local}"
} >> "${GITHUB_OUTPUT:-/dev/stdout}"
printf 'Chrona %s: versionName %s, versionCode %s\n' "$variant" "$name" "$code"
