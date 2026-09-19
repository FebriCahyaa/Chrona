<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Security Policy

## Reporting a vulnerability

Do not publish security vulnerabilities in a public issue.

Use a private GitHub Security Advisory for the `FebriCahyaa/Chrona` repository whenever that feature is available. Include the affected version/commit, impact, reproduction steps, and a minimal proof of concept that does not expose personal data or production credentials.

If private advisories are unavailable, contact the repository owner through the private communication channel listed in the repository profile.

## Secrets

Never commit Telegram tokens, GitHub tokens, Android signing keys, keystores, API credentials, or personal access tokens. CI reads secrets from GitHub Actions secrets/environments and never writes them into the repository.

## Security automation

Chrona uses:

- GitHub Dependency Review on pull requests.
- CodeQL for Kotlin/Java and native C++.
- OpenSSF Scorecard on the default branch/scheduled audits.
- Dependabot for Gradle and GitHub Actions updates.
- Artifact provenance attestations for published release binaries.

## Supported releases

The default branch is the primary security-supported development line. Older releases are not guaranteed to receive security fixes unless explicitly stated in their release notes.
