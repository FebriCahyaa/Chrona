#!/usr/bin/env bash
set -euo pipefail
root_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
keystore="$root_dir/chrona-release.keystore"
alias_name="chrona-release"
store_type="PKCS12"
if [[ -e "$keystore" ]]; then
    printf 'Release keystore already exists: %s\n' "$keystore"
    exit 1
fi
command -v keytool >/dev/null 2>&1 || { printf 'keytool is required.\n' >&2; exit 1; }
read -r -s -p 'Keystore password: ' store_password
printf '\n'
read -r -s -p 'Key password: ' key_password
printf '\n'
keytool -genkeypair -v -storetype "$store_type" -keystore "$keystore" -alias "$alias_name" -keyalg RSA -keysize 4096 -validity 10000 -storepass "$store_password" -keypass "$key_password" -dname 'CN=Chrona Release, O=FebriCahyaa, C=ID'
chmod 600 "$keystore"
printf 'Generated: %s\n' "$keystore"
