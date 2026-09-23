# Chrona Telegram Automation

Chrona uses four Telegram bots with dedicated repository secrets and a shared Python notification engine.

| Bot | Secrets | Responsibility |
| --- | --- | --- |
| CI | `CHRONABOT_CI_TOKEN`, `CHRONABOT_CI_CHAT_ID` | CI start, quality/build result, artifacts, error extracts, security status |
| Dependabot | `CHRONABOT_DEPENDABOT_TOKEN`, `CHRONABOT_DEPENDABOT_CHAT_ID` | Dependabot pull requests and dependency maintenance events |
| PR | `CHRONABOT_PR_TOKEN`, `CHRONABOT_PR_CHAT_ID` | Pull requests, issue tracker, localization automation |
| Release | `CHRONABOT_RELEASE_TOKEN`, `CHRONABOT_RELEASE_CHAT_ID` | Release preparation, signed APK, checksums, release publication |

The notifier is `scripts/telegram/notify.py` and uses only Python standard library modules. Workflows select the bot by mapping the correct repository secret pair into `TELEGRAM_BOT_TOKEN` and `TELEGRAM_CHAT_ID`.

## CI notifications

The CI workflow emits a start message before the build, then a final status containing quality and build states. Error extracts are embedded in the message when available. APK files, error extracts and SARIF reports are uploaded to Telegram when their size is within the configured transfer limit.

## Release notifications

The release workflow generates release notes, builds the signed APK, writes SHA-256 checksums, creates a provenance attestation, publishes the GitHub Release and forwards the release APK, checksum file and build logs to the release bot.

## Pull request notifications

The PR bot reports opened, synchronized, reopened, ready-for-review and closed events. The message includes author, source branch, target branch, line additions, deletions and changed-file count.

## Dependabot notifications

Dependabot pull requests are routed to the dedicated Dependabot bot instead of the normal PR bot. This keeps dependency maintenance separate from application development traffic.

## Issue tracker notifications

Issue opened, reopened and closed events are routed to the PR bot. The message contains issue number, title, author and labels with a direct issue button.

## Security notifications

The CI bot receives the status of CodeQL, dependency review and dependency graph submission. CodeQL currently uses the supported v4 action line. GitHub documents v4 as the latest supported CodeQL Action major. 

## Telegram interface

Messages use HTML formatting and inline URL buttons. Telegram's current Bot API documents `InlineKeyboardMarkup` and button styles including `primary`, `success` and `danger`, allowing the notification UI to distinguish navigation and release or failure actions.

## Future command mode

Command and callback-driven bot controls should run from a persistent service or other continuously available runtime. GitHub Actions is suitable for event-driven notifications and build automation, but it is not a continuously running Telegram webhook server.

Potential command features include build dispatch, workflow status lookup, release lookup, latest artifact lookup, failed-job log retrieval, issue search, PR search, dependency update summary, security status and Crowdin synchronization status.
