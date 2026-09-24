#!/usr/bin/env bash
# Decode ANDROID_KEYSTORE_BASE64 into RUNNER_TEMP, validate it with
# ANDROID_KEYSTORE_PASSWORD and export ANDROID_KEYSTORE_FILE for Gradle.
#
#   usage: materialize_keystore.sh [--optional]
#
# With --optional, missing secrets are not an error (canary falls back to the
# debug key). Without it, both secrets are required (release builds).
set -euo pipefail

optional=false
[[ "${1:-}" == --optional ]] && optional=true

if [[ -z "${ANDROID_KEYSTORE_BASE64:-}" || -z "${ANDROID_KEYSTORE_PASSWORD:-}" ]]; then
    if [[ "$optional" == true ]]; then
        printf '::notice::Release keystore secrets are not available; signing with the debug key.\n'
        exit 0
    fi
    printf '::error::ANDROID_KEYSTORE_BASE64 and ANDROID_KEYSTORE_PASSWORD are required.\n' >&2
    exit 1
fi

keystore="${RUNNER_TEMP:?}/chrona-release.p12"
printf '%s' "$ANDROID_KEYSTORE_BASE64" | base64 --decode > "$keystore"
chmod 600 "$keystore"
if ! keytool -list -storetype PKCS12 -keystore "$keystore" \
    -storepass "$ANDROID_KEYSTORE_PASSWORD" -alias chrona-release > /dev/null; then
    printf '::error::Release keystore validation failed. Check ANDROID_KEYSTORE_BASE64 and ANDROID_KEYSTORE_PASSWORD.\n' >&2
    exit 1
fi
printf 'ANDROID_KEYSTORE_FILE=%s\n' "$keystore" >> "${GITHUB_ENV:?}"
printf 'Release keystore validated.\n'
