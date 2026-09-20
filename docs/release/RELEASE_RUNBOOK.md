<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Release Runbook

1. Update `versionName` and `versionCode` in a reviewed source commit.
2. Run `testOssDebugUnitTest`, `lintOssDebug`, and a native Debug build on a networked Android build host.
3. Create a version tag such as `v0.5.1` matching `versionName`.
4. Confirm release signing secrets are configured in the `release` GitHub Environment.
5. Open **Chrona Release Build** through `workflow_dispatch`.
6. Enter the existing tag and choose `publish=true` only when the GitHub Release should be created.
7. Inspect the uploaded APK/AAB, SHA-256 checksum and provenance attestation.
8. Release Telegram notification runs only when publication is enabled and the dedicated Telegram credentials are configured.

The release workflow is manual-only. It does not receive a tag push trigger, modify application source, push commits to `main`, or create ZIP flashables.
