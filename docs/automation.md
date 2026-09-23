# Chrona Automation

Chrona automation is intentionally consolidated into four workflow files so each file can own multiple related jobs without creating a large collection of tiny workflows.

## Workflow layout

`ci.yml` owns CI start notification, quality checks, debug build, reports and Telegram delivery.

`release.yml` owns release validation, changelog generation, signing, APK packaging, checksum generation, provenance attestation, GitHub Release publication and release Telegram delivery.

`automation.yml` owns pull request notifications, Dependabot notifications, issue tracker notifications, PR labels, changelog generation on demand, Crowdin synchronization and label bootstrap.

`security.yml` owns CodeQL, dependency review, dependency graph submission and security Telegram delivery.

## Repository secrets

Telegram secrets are intentionally split by bot responsibility. The workflows only receive the token and chat ID needed by the current job.

Release signing secrets belong in the `release` environment. The release workflow materializes the keystore under `RUNNER_TEMP` and never writes it back into the repository.

Crowdin credentials remain separate from Telegram credentials.

See `docs/telegram-automation.md` for the notification matrix and bot responsibilities.
