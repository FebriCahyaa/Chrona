# Chrona

Chrona is an Android clock application derived from the AOSP DeskClock lineage and maintained as an independently engineered project under the FebriCahyaa repository.

## Build paths

Chrona preserves `Android.bp` for AOSP/Soong integration and adds a Gradle build for standalone Android builds and GitHub Actions.

```text
Gradle 9.7.1
Android Gradle Plugin 9.4.0
Kotlin 2.4.20
JDK 17
Android API 36
```

Run `./gradlew assembleDebug`, `./gradlew lintDebug`, and `./gradlew testDebugUnitTest`. CI provisions Android API 36 and Build Tools 36.0.0 explicitly.

## Automation

The repository includes separate workflows for CI, releases, security, changelog generation, Crowdin synchronization, label management, and Telegram event delivery. Build logs, error excerpts, APK artifacts, release checksums, provenance, and release status are published through the configured automation paths.

## Telegram

Set `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID` repository or environment secrets. The notifier uses Telegram Bot API inline URL buttons and can upload APKs, checksums, and compact build-error extracts.

## Crowdin

Set `CROWDIN_PROJECT_ID` as a repository variable and `CROWDIN_PERSONAL_TOKEN` as a repository or organization secret. Translation synchronization creates a dedicated pull request.

## Google assets

`assets/fonts` contains the asset contract and a refresh script for Google Sans Flex and Material Symbols Rounded. Run `python3 scripts/assets/fetch_google_assets.py` on a network-enabled machine or CI runner to fetch the current upstream binaries.

## Release signing

Run `scripts/release/generate_release_key.sh` on a trusted local machine to generate `chrona-release.keystore` at the repository root. The generated key is ignored by Git. GitHub release builds use an ephemeral runner copy supplied through protected secrets.

## Security and licensing

Release signing material is private and must not be committed or uploaded. Copyright and attribution are documented in `COPYRIGHT.md`, `NOTICE`, `THIRD_PARTY_NOTICES.md`, and the original source headers.
