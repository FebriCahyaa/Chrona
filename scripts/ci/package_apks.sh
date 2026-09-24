#!/usr/bin/env bash
# Copy the five ABI/universal APKs into OUTPUT_DIR with a versioned prefix
# and write SHA256SUMS.txt next to them.
#
#   usage: package_apks.sh APK_DIR OUTPUT_DIR PREFIX
set -euo pipefail

apk_dir="${1:?usage: package_apks.sh APK_DIR OUTPUT_DIR PREFIX}"
out_dir="${2:?usage: package_apks.sh APK_DIR OUTPUT_DIR PREFIX}"
prefix="${3:?usage: package_apks.sh APK_DIR OUTPUT_DIR PREFIX}"

shopt -s nullglob
apks=("$apk_dir"/*.apk)
if (( ${#apks[@]} != 5 )); then
    printf '::error::Expected 5 APKs (4 ABI + universal) in %s, found %s.\n' "$apk_dir" "${#apks[@]}" >&2
    exit 1
fi

mkdir -p "$out_dir"
for apk in "${apks[@]}"; do
    # app-arm64-v8a-canary.apk -> PREFIX-arm64-v8a.apk
    abi="$(basename "$apk" .apk)"
    abi="${abi#app-}"
    abi="${abi%-*}"
    cp "$apk" "$out_dir/${prefix}-${abi}.apk"
done
(cd "$out_dir" && sha256sum ./*.apk | sed 's# \./# #' > SHA256SUMS.txt)
ls -l "$out_dir"
