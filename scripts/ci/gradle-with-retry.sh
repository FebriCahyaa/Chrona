#!/usr/bin/env bash
# Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
#
# Retry Gradle only for transient repository/network failures. Deterministic
# build/test/lint failures are returned immediately so CI does not hide real
# regressions.
set -u -o pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

MAX_ATTEMPTS="${GRADLE_RETRY_MAX_ATTEMPTS:-4}"
INITIAL_DELAY="${GRADLE_RETRY_INITIAL_DELAY_SECONDS:-15}"

if ! [[ "$MAX_ATTEMPTS" =~ ^[1-9][0-9]*$ ]] || ! [[ "$INITIAL_DELAY" =~ ^[0-9]+$ ]]; then
  echo "Invalid retry configuration" >&2
  exit 2
fi

if [[ $# -eq 0 ]]; then
  echo "Usage: $0 <gradle arguments...>" >&2
  exit 2
fi

TMP_OUTPUT="$(mktemp)"
trap 'rm -f "$TMP_OUTPUT"' EXIT

for ((attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)); do
  : > "$TMP_OUTPUT"
  echo "Gradle attempt ${attempt}/${MAX_ATTEMPTS}: ./gradlew $*" >&2

  set +e
  ./gradlew "$@" 2>&1 | tee "$TMP_OUTPUT"
  status=${PIPESTATUS[0]}
  set -e

  if (( status == 0 )); then
    exit 0
  fi

  if ! grep -Eiq \
      '429|Too Many Requests|HTTP 5[0-9][0-9]|status code 5[0-9][0-9]|Could not (GET|HEAD|POST)|Connection (timed out|reset|refused)|UnknownHostException|Temporary failure in name resolution|Network is unreachable' \
      "$TMP_OUTPUT"; then
    echo "Gradle failed with a non-transient error; not retrying." >&2
    exit "$status"
  fi

  if (( attempt == MAX_ATTEMPTS )); then
    echo "Gradle failed after ${MAX_ATTEMPTS} transient-failure retries." >&2
    exit "$status"
  fi

  delay=$(( INITIAL_DELAY * (2 ** (attempt - 1)) ))
  echo "Transient repository/network failure detected; retrying in ${delay}s..." >&2
  sleep "$delay"
done

exit 1
