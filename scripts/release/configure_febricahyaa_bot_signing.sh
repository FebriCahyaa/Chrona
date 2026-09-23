#!/usr/bin/env bash
set -euo pipefail

root_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)
private_key="$root_dir/chronabot-gpg-private.asc"
bot_name='FebriCahyaa Bot'
bot_email='87246800+FebriCahyaa@users.noreply.github.com'
author_name='FebriCahyaa'
author_email='febricahya12345@gmail.com'

gpg --version >/dev/null 2>&1 || {
    printf 'gpg is required. Install GnuPG first.\n' >&2
    exit 1
}

git -C "$root_dir" rev-parse --show-toplevel >/dev/null 2>&1 || {
    printf 'This script must be run inside the Chrona Git repository.\n' >&2
    exit 1
}

[[ -f "$private_key" ]] || {
    printf 'Missing %s. Generate the key first with scripts/release/generate_github_bot_gpg.sh.\n' "$private_key" >&2
    exit 1
}

gpg --import "$private_key" >/dev/null 2>&1
fingerprint=$(gpg --with-colons --list-secret-keys "$bot_email" | awk -F: '$1 == "fpr" { print $10; exit }')
[[ "$fingerprint" =~ ^[A-F0-9]{40}$ ]] || {
    printf 'Unable to locate the imported GPG signing key for %s.\n' "$bot_email" >&2
    exit 1
}

git -C "$root_dir" config user.name "$bot_name"
git -C "$root_dir" config user.email "$bot_email"
git -C "$root_dir" config user.signingkey "$fingerprint"
git -C "$root_dir" config commit.gpgsign true
git -C "$root_dir" config tag.gpgSign true

gpg --list-secret-keys "$fingerprint" >/dev/null 2>&1 || {
    printf 'GPG secret key lookup failed after import.\n' >&2
    exit 1
}

printf 'Git signing configured.\n'
printf 'Committer: %s <%s>\n' "$bot_name" "$bot_email"
printf 'Author default for automation commits: %s <%s>\n' "$author_name" "$author_email"
printf 'Signing key: %s\n' "$fingerprint"
printf 'Future commits and tags will be signed by default.\n'
printf 'To preserve the FebriCahyaa author while keeping the bot as committer, use:\n'
printf 'GIT_AUTHOR_NAME=%q GIT_AUTHOR_EMAIL=%q git commit -S -m "your message"\n' "$author_name" "$author_email"
printf 'Inspect with: git log --show-signature -1\n'
