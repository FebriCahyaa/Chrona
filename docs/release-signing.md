# Chrona Release Signing

Chrona release signing uses a PKCS12 keystore with one password. The alias is fixed to `chrona-release`. GitHub Actions therefore requires exactly two repository or environment secrets.

## 1. Generate the release key

From the Chrona repository:

```sh
./scripts/release/generate_release_key.sh
```

Enter one password and confirm it. The script creates:

- `chrona-release.p12` — the private release keystore.
- `chrona-release.p12.base64.txt` — the Base64 representation used by GitHub Actions.

Both files are ignored by Git. Keep them private and back them up securely.

## 2. Configure GitHub Actions

Open the repository's **Settings → Secrets and variables → Actions** and create these two secrets in the `release` environment used by the release workflow:

`ANDROID_KEYSTORE_BASE64`

Set its value to the complete single-line contents of `chrona-release.p12.base64.txt`.

`ANDROID_KEYSTORE_PASSWORD`

Set its value to the exact password entered during key generation. Do not add quotes or spaces.

No `ANDROID_KEY_ALIAS` or `ANDROID_KEY_PASSWORD` secret is required.

## 3. How the workflow validates the key

Before `assembleRelease`, the workflow decodes the Base64 value, opens the resulting PKCS12 keystore with `ANDROID_KEYSTORE_PASSWORD`, and verifies the `chrona-release` alias. A wrong password, corrupted Base64 value, missing alias, or mismatched secret now fails at this validation step with a direct error.

## 4. Important

Do not regenerate the release key after publishing an application unless you intentionally want to change the signing identity. Existing signed builds must continue using the same release key.
