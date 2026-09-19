<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Telegram Bot Notifications

Chrona uses four independent Telegram bots/channels so operational messages remain separated by concern.

| Bot | Secret token | Chat ID | Responsibility |
| --- | --- | --- | --- |
| CI Bot | `CHRONABOT_CI_TOKEN` | `CHRONABOT_CI_CHAT_ID` | GitHub Actions workflow results |
| PR Bot | `CHRONABOT_PR_TOKEN` | `CHRONABOT_PR_CHAT_ID` | Pull request opened/updated/closed events |
| Dependabot Bot | `CHRONABOT_DEPENDABOT_TOKEN` | `CHRONABOT_DEPENDABOT_CHAT_ID` | Dependabot pull requests |
| Release Bot | `CHRONABOT_RELEASE_TOKEN` | `CHRONABOT_RELEASE_CHAT_ID` | Published/pre-released GitHub releases |

Create the bots through Telegram's BotFather, add each bot to the intended private/group chat, and record the chat ID in GitHub Actions secrets. Never place a token in YAML or source code.

The bots are intentionally non-overlapping:

- CI Bot reports workflow-run results for push/scheduled/manual automation.
- PR Bot reports ordinary pull-request lifecycle events.
- Dependabot Bot reports Dependabot pull requests only.
- Release Bot reports published/pre-released GitHub releases only.

PR-triggered CI runs are intentionally excluded from the CI Bot to avoid duplicate messages; PR/Dependabot bots remain the notification surface for those events.

## Message design

All messages are HTML-formatted, concise, include the commit SHA/title where available, and include one link to the corresponding GitHub object. The Bot API accepts UTF-8 HTTP requests and `sendMessage` is used over HTTPS; Chrona also enforces Telegram's 4096-character message limit in the renderer.

The implementation is [`scripts/telegram/notify.py`](../../scripts/telegram/notify.py). It intentionally uses Python's standard library only, so CI does not install another runtime dependency.

## Message templates

### CI Bot
```text
✅ Chrona CI Update
Workflow: Chrona CI
Status: success
Event: push
Branch: main
Commit: abcdef123456
Commit title: fix(ui): ...
Run: #123
Duration: 87s
Open Actions run
```

### Pull Request Bot
```text
🔔 Chrona Pull Request
Action: opened
PR: #123
Title: feat(worldclock): ...
Commit: abcdef123456
Commit title: feat(worldclock): ...
Base: main
State: open
Author: contributor
Open Pull Request
```

### Dependabot Bot
```text
🤖 Chrona Dependabot
Action: synchronize
PR: #456
Title: Bump ...
Commit: abcdef123456
Commit title: chore(deps): ...
Base: main
State: open
Author: dependabot[bot]
Open Pull Request
```

### Release Bot
```text
🚀 Chrona Release
Version: v0.6.0
Title: Chrona v0.6.0
Commit: abcdef123456
Commit title: release: Chrona v0.6.0
Open Release
```

The CI bot is deliberately excluded from pull-request workflow runs so a PR does not produce a second CI notification beside the PR/Dependabot bot. Release messages are emitted only from GitHub `release` events, so release announcements do not share the CI channel.

## GitHub secrets

Configure these eight repository/environment secrets; never commit their values:

`CHRONABOT_CI_TOKEN`, `CHRONABOT_CI_CHAT_ID`, `CHRONABOT_PR_TOKEN`, `CHRONABOT_PR_CHAT_ID`, `CHRONABOT_DEPENDABOT_TOKEN`, `CHRONABOT_DEPENDABOT_CHAT_ID`, `CHRONABOT_RELEASE_TOKEN`, and `CHRONABOT_RELEASE_CHAT_ID`.
