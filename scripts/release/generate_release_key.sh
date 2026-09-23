#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
keystore="$root_dir/chrona-release.p12"
base64_file="$root_dir/chrona-release.p12.base64.txt"
alias_name="chrona-release"
store_type="PKCS12"

aftercare() {
    if [[ -n "${store_password:-}" ]]; then
        unset store_password
    fi
}
trap aftercare EXIT

if [[ -e "$keystore" || -e "$base64_file" ]]; then
    printf 'Release signing material already exists.\n' >&2
    printf 'Keystore: %s\n' "$keystore" >&2
    printf 'Base64:   %s\n' "$base64_file" >&2
    printf 'Remove the existing local files manually only when you intentionally want a new release key.\n' >&2
    exit 1
fi

command -v keytool >/dev/null 2>&1 || { printf 'keytool is required.\n' >&2; exit 1; }
command -v base64 >/dev/null 2>&1 || { printf 'base64 is required.\n' >&2; exit 1; }

read -r -s -p 'Release keystore password: ' store_password
printf '\n'
read -r -s -p 'Confirm release keystore password: ' store_password_confirm
printf '\n'
if [[ "$store_password" != "$store_password_confirm" ]]; then
    unset store_password_confirm
    printf 'Passwords do not match. No keystore was created.\n' >&2
    exit 1
fi
unset store_password_confirm

if [[ -z "$store_password" ]]; then
    printf 'Password must not be empty.\n' >&2
    exit 1
fi

umask 077
keytool -genkeypair \
    -storetype "$store_type" \
    -keystore "$keystore" \
    -alias "$alias_name" \
    -keyalg RSA \
    -keysize 4096 \
    -validity 10000 \
    -storepass "$store_password" \
    -keypass "$store_password" \
    -dname 'CN=Chrona Release, O=FebriCahyaa, C=ID' \
    -noprompt

keytool -list \
    -storetype "$store_type" \
    -keystore "$keystore" \
    -storepass "$store_password" \
    -alias "$alias_name" \
    >/dev/null

base64 -w 0 "$keystore" > "$base64_file"
printf '\n' >> "$base64_file"
chmod 600 "$keystore" "$base64_file"

printf '\nRelease key generated successfully.\n'
printf 'Keystore: %s\n' "$keystore"
printf 'Base64:   %s\n' "$base64_file"
printf 'Alias:    %s\n' "$alias_name"
printf 'Type:     %s\n' "$store_type"
printf '\nGitHub Actions Secrets (exactly 2):\n'
printf '1. ANDROID_KEYSTORE_BASE64 = contents of %s\n' "$base64_file"
printf '2. ANDROID_KEYSTORE_PASSWORD = the password you just entered\n'
printf '\nDo not commit either the .p12 file or the .base64.txt file.\n'
