# Push Chrona multi-commit history

This archive contains a complete Git repository with four commits on `main` and the remote set to `https://github.com/FebriCahyaa/Chrona.git`.

After extracting the archive, run `git log --oneline --decorate --graph` to verify the history.

Authenticate with GitHub, then run `git push -u origin main`.

Do not upload the ZIP itself through GitHub's web file uploader if preserving the four commits is required; extract the archive first and push the repository from the extracted directory.

## Signed commits with FebriCahyaa Bot

Automation commits use `FebriCahyaa <febricahya12345@gmail.com>` as author and `FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>` as committer and signing identity.

Generate the signing key locally:

```sh
./scripts/release/generate_github_bot_gpg.sh
```

Add `chronabot-gpg-public.asc` to **GitHub → Settings → SSH and GPG keys → New GPG key**. The email `87246800+FebriCahyaa@users.noreply.github.com` must be verified on the GitHub account that owns the key.

For local signed commits:

```sh
./scripts/release/configure_febricahyaa_bot_signing.sh
git status
git add .
GIT_AUTHOR_NAME='FebriCahyaa' GIT_AUTHOR_EMAIL='febricahya12345@gmail.com' git commit -S -m "ci: add FebriCahyaa Bot signed automation"
git log --show-signature -1
```

For GitHub Actions, create these repository Actions secrets:

```text
CHRONABOT_GPG_PRIVATE_KEY
CHRONABOT_GPG_PASSPHRASE
```

`CHRONABOT_GPG_PRIVATE_KEY` must contain the complete ASCII-armored contents of `chronabot-gpg-private.asc`. Do not paste the Base64 representation into the Crowdin Action input.
