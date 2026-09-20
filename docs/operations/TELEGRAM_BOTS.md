<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Chrona Telegram Operations

Telegram automation is intentionally implemented inside the five primary GitHub Actions workflows.

## Credentials

| Surface | Secrets |
| --- | --- |
| Update/CI | `CHRONABOT_CI_TOKEN`, `CHRONABOT_CI_CHAT_ID` |
| Pull Request / Dependabot | `CHRONABOT_PR_TOKEN`, `CHRONABOT_PR_CHAT_ID` |
| Release | `CHRONABOT_RELEASE_TOKEN`, `CHRONABOT_RELEASE_CHAT_ID` |

No token or chat ID belongs in source code or YAML literals.

## Update / CI message

The renderer includes workflow, status, event, branch, commit, commit message, author and a compact parent-to-head change summary.

## Pull Request message

The renderer includes action, PR number, title, base/head, commit, author and a compact change summary. Dependabot uses the same workflow/event path but a dedicated heading so dependency updates remain visually distinct.

## Release message

The renderer includes release tag, status, commit, author and recent commit titles with a direct GitHub Release link.

## Failure handling

Every workflow checks whether the corresponding Telegram secrets exist before attempting a send. Missing notification credentials skip only the notification step; they do not turn a code/build validation job into a failure.

## Transport

`scripts/telegram/notify.py` uses Python's standard library and the Telegram Bot API. HTML text is escaped, the 4096-character Telegram limit is enforced, and external HTTP failures are surfaced as explicit process errors.
