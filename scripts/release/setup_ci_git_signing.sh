#!/usr/bin/env bash
set -euo pipefail

bot_name='FebriCahyaa Bot'
bot_email='87246800+FebriCahyaa@users.noreply.github.com'

: "${CHRONABOT_GPG_PRIVATE_KEY:?CHRONABOT_GPG_PRIVATE_KEY is required}"
: "${CHRONABOT_GPG_PASSPHRASE:?CHRONABOT_GPG_PASSPHRASE is required}"

command -v gpg >/dev/null 2>&1 || { printf 'gpg is required.\n' >&2; exit 1; }
command -v git >/dev/null 2>&1 || { printf 'git is required.\n' >&2; exit 1; }

gpg_home="${GNUPGHOME:-$RUNNER_TEMP/chronabot-gnupg}"
passphrase_file="$RUNNER_TEMP/chronabot-gpg-passphrase"
gpg_wrapper="$RUNNER_TEMP/chronabot-gpg-wrapper"
private_key_file="$RUNNER_TEMP/chronabot-gpg-private.asc"

mkdir -p "$gpg_home"
chmod 700 "$gpg_home"
printf '%s' "$CHRONABOT_GPG_PASSPHRASE" > "$passphrase_file"
printf '%s\n' "$CHRONABOT_GPG_PRIVATE_KEY" > "$private_key_file"
chmod 600 "$passphrase_file" "$private_key_file"
export GNUPGHOME="$gpg_home"

gpg --batch --import "$private_key_file" >/dev/null
fingerprint=$(gpg --with-colons --list-secret-keys "$bot_email" | awk -F: '$1 == "fpr" { print $10; exit }')
[[ "$fingerprint" =~ ^[A-F0-9]{40}$ ]] || {
    printf 'Unable to locate the FebriCahyaa Bot GPG key for %s.\n' "$bot_email" >&2
    exit 1
}

cat > "$gpg_wrapper" <<WRAPPER
#!/usr/bin/env bash
exec gpg --homedir "$gpg_home" --batch --pinentry-mode loopback --passphrase-file "$passphrase_file" "\$@"
WRAPPER
chmod 700 "$gpg_wrapper"

git config user.name "$bot_name"
git config user.email "$bot_email"
git config user.signingkey "$fingerprint"
git config gpg.program "$gpg_wrapper"
git config commit.gpgsign true
git config tag.gpgSign true

gpg --batch --list-secret-keys "$fingerprint" >/dev/null 2>&1 || {
    printf 'Unable to verify the imported FebriCahyaa Bot signing key.\n' >&2
    exit 1
}

printf 'CI Git signing configured successfully.\n'
printf 'Committer: %s <%s>\n' "$bot_name" "$bot_email"
printf 'Signing fingerprint: %s\n' "$fingerprint"
