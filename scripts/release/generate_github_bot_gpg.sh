#!/usr/bin/env bash
set -euo pipefail

root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
gpg_home="$root_dir/.generated/gpg"
private_key="$root_dir/chronabot-gpg-private.asc"
public_key="$root_dir/chronabot-gpg-public.asc"
private_b64="$root_dir/chronabot-gpg-private.base64.txt"
fingerprint_file="$root_dir/chronabot-gpg-fingerprint.txt"
name="FebriCahyaa Bot"
email="87246800+FebriCahyaa@users.noreply.github.com"

if [[ -e "$private_key" || -e "$public_key" || -e "$private_b64" || -e "$fingerprint_file" ]]; then
    printf 'Existing generated GPG material was found. Refusing to overwrite it.\n' >&2
    exit 1
fi

command -v gpg >/dev/null 2>&1 || {
    printf 'gpg is required. Install GnuPG first.\n' >&2
    exit 1
}

command -v base64 >/dev/null 2>&1 || {
    printf 'base64 is required.\n' >&2
    exit 1
}

mkdir -p "$gpg_home"
chmod 700 "$gpg_home"

tmp_passphrase=$(mktemp)
cleanup() {
    rm -f "$tmp_passphrase"
    rm -rf "$gpg_home"
}
trap cleanup EXIT

read -r -s -p 'FebriCahyaa Bot GPG passphrase: ' passphrase
printf '\n'
read -r -s -p 'Confirm GPG passphrase: ' passphrase_confirm
printf '\n'
[[ -n "$passphrase" ]] || { printf 'Passphrase cannot be empty.\n' >&2; exit 1; }
[[ "$passphrase" == "$passphrase_confirm" ]] || { printf 'Passphrases do not match.\n' >&2; exit 1; }
printf '%s' "$passphrase" > "$tmp_passphrase"
chmod 600 "$tmp_passphrase"
unset passphrase passphrase_confirm

GNUPGHOME="$gpg_home" gpg --batch --pinentry-mode loopback --passphrase-file "$tmp_passphrase" --quick-generate-key "$name <$email>" rsa4096 sign 5y

fingerprint=$(GNUPGHOME="$gpg_home" gpg --with-colons --list-secret-keys "$email" | awk -F: '$1 == "fpr" { print $10; exit }')
[[ "$fingerprint" =~ ^[A-F0-9]{40}$ ]] || { printf 'Unable to determine generated GPG fingerprint.\n' >&2; exit 1; }

GNUPGHOME="$gpg_home" gpg --batch --pinentry-mode loopback --passphrase-file "$tmp_passphrase" --armor --export-secret-keys "$fingerprint" > "$private_key"
GNUPGHOME="$gpg_home" gpg --batch --armor --export "$fingerprint" > "$public_key"
base64 -w 0 "$private_key" > "$private_b64"
printf '\n' >> "$private_b64"
printf '%s\n' "$fingerprint" > "$fingerprint_file"

chmod 600 "$private_key" "$private_b64"
chmod 644 "$public_key" "$fingerprint_file"

printf '\nFebriCahyaa Bot GPG signing key generated successfully.\n'
printf 'Fingerprint: %s\n' "$fingerprint"
printf 'Signing UID: %s <%s>\n' "$name" "$email"
printf 'Private key: %s\n' "$private_key"
printf 'Public key: %s\n' "$public_key"
printf 'Base64 backup: %s\n' "$private_b64"
printf 'Fingerprint file: %s\n' "$fingerprint_file"
printf '\nGitHub setup:\n'
printf '1. Add the complete contents of %s to GitHub Settings > SSH and GPG keys > New GPG key.\n' "$public_key"
printf '2. Confirm %s is a verified email on the GitHub account that owns the key.\n' "$email"
printf '3. Create Actions secret CHRONABOT_GPG_PRIVATE_KEY using the complete ASCII-armored contents of %s.\n' "$private_key"
printf '4. Create Actions secret CHRONABOT_GPG_PASSPHRASE using the passphrase you entered.\n'
printf '5. Never commit, publish, or upload the private key.\n'
printf '\nAutomation identity:\n'
printf 'Author:    FebriCahyaa <febricahya12345@gmail.com>\n'
printf 'Committer: FebriCahyaa Bot <%s>\n' "$email"
printf 'Signer:    FebriCahyaa Bot <%s>\n' "$email"
