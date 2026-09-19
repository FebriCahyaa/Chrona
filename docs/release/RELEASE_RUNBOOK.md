<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Release Runbook

1. Update `versionName` and `versionCode` in a reviewed source commit.
2. Run unit tests, lint, and device QA.
3. Create a version tag such as `v0.5.1` matching `versionName`.
4. Confirm release signing secrets are configured in the `release` GitHub Environment.
5. Let `release.yml` build the signed APK and AAB.
6. Verify APK signature and SHA-256 checksums.
7. Publish the GitHub Release.
8. Release Telegram Bot sends a separate publication notification.

The release workflow does not modify application source or push commits back to the repository.
