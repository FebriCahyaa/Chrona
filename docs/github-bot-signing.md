# FebriCahyaa Bot Git Signing

Chrona uses a dedicated GPG signing identity named `FebriCahyaa Bot` for commits created by automation.

The identities are intentionally separated:

```text
Author:    FebriCahyaa <febricahya12345@gmail.com>
Committer: FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
Signer:    FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
```

The GitHub-provided no-reply address is used on the GPG identity and as the committer address so GitHub can associate the signature with the `FebriCahyaa` account. The human author remains the Gmail address above.

## Generate the signing key

Run this locally in a trusted workspace:

```sh
./scripts/release/generate_github_bot_gpg.sh
```

The script creates:

- `chronabot-gpg-private.asc` — private key; keep secret.
- `chronabot-gpg-public.asc` — public key for GitHub.
- `chronabot-gpg-private.base64.txt` — optional backup representation.
- `chronabot-gpg-fingerprint.txt` — fingerprint.

The generated private key is intentionally ignored by Git.

## Add the public key to GitHub

Open **GitHub → Settings → SSH and GPG keys → New GPG key** and paste the complete contents of `chronabot-gpg-public.asc`.

The GitHub account that owns the key must have this verified email address:

```text
87246800+FebriCahyaa@users.noreply.github.com
```

## Add GitHub Actions secrets

Create these repository Actions secrets:

```text
CHRONABOT_GPG_PRIVATE_KEY
CHRONABOT_GPG_PASSPHRASE
```

`CHRONABOT_GPG_PRIVATE_KEY` must contain the complete ASCII-armored contents of `chronabot-gpg-private.asc`.

`CHRONABOT_GPG_PASSPHRASE` must contain exactly the passphrase entered during key generation.

## Why Crowdin is handled manually

The official Crowdin GitHub Action supports GPG-signed commits, but its documented signing configuration requires the commit email, GPG key identity email, and GitHub account email to match. Chrona needs a different author and committer, so the workflow uses Crowdin only to download translations and then creates the Git commit itself.

The workflow configures:

```text
Author:    FebriCahyaa <febricahya12345@gmail.com>
Committer: FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
Signer:    FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
```

It then pushes `crowdin-sync` and creates the pull request with the GitHub CLI.

## Configure local signing

After copying `chronabot-gpg-private.asc` into the repository root:

```sh
./scripts/release/configure_febricahyaa_bot_signing.sh
```

The script configures `FebriCahyaa Bot` as the committer and signing identity. To create a commit with the human author preserved, run:

```sh
GIT_AUTHOR_NAME='FebriCahyaa' \
GIT_AUTHOR_EMAIL='febricahya12345@gmail.com' \
git commit -S -m 'your message'
```

Inspect the result with:

```sh
git log --show-signature -1
```

## Verification

GitHub checks the committer email against the identities on the GPG key and requires the email to be verified on the associated account. The author and committer are separate Git metadata fields, so an automation commit can retain `FebriCahyaa` as author while using `FebriCahyaa Bot` as the committer and signing identity.

Existing unsigned commits do not become signed automatically. This setup signs future commits only.
