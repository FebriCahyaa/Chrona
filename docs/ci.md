# CI and Branching

## Branches

| Branch | Purpose | Workflow | Output |
| --- | --- | --- | --- |
| `main` | Integration branch; every change lands here first | Build (`debug`) | Debug APKs (artifact) |
| `dev` | Development line promoted from `main` | Build (`dev`) | Dev APKs (artifact) |
| `canary` | Pre-release line promoted from `dev` | Build (`canary`) | Canary APKs + GitHub pre-release |
| `stable` | Release line promoted from `canary` | Release | Signed APKs; GitHub Release on `v*` tags |

Changes flow `main → dev → canary → stable`. Run **Automation → Run workflow →
action: promote** to open the promotion pull request, and merge it with a
merge commit so the branches keep a shared history.

## Workflows

Four workflow files, each covering one concern with several jobs, instead of
one file per build line or task:

| File | Trigger | Jobs |
| --- | --- | --- |
| `build.yml` | push `main`/`dev`/`canary`, pull requests, manual | resolve variant from branch → quality, APK build (SDK channel 1/2/3), canary pre-release via gh-release, Telegram start/report |
| `release.yml` | push `stable`, `v*` tags, pull requests, manual | PR autolabel, release draft (Release Drafter), signed Release APK (SDK channel 0), attestation, gh-release, Telegram |
| `checks.yml` | push, pull requests, weekly, manual | Super-Linter, CodeQL, TruffleHog, dependency review, dependency graph, Telegram |
| `automation.yml` | PRs, issues, schedule, manual | Telegram notifications, PR labels, changelog, Crowdin sync (signed commit), metrics render (signed commit), branch promotion |

All Android jobs use the composite action `.github/actions/android-toolchain`
(JDK 17, SDK for a channel, Gradle). `build.yml` can be run manually with a
`variant` input (`auto` derives it from the branch) and optional `platform` /
`build-tools` pins.

## Bot-signed automation commits

Commits that CI makes to the repository (Crowdin sync, the `metrics` branch)
are GPG-signed by **FebriCahyaa Bot**, not the default `github-actions[bot]`
identity:

```text
Author:    FebriCahyaa <febricahya12345@gmail.com>
Committer: FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
Signer:    FebriCahyaa Bot <87246800+FebriCahyaa@users.noreply.github.com>
```

The composite action `.github/actions/bot-signing` configures this (backed by
`scripts/release/setup_ci_git_signing.sh`); a step then runs `git commit -S`
with `GIT_AUTHOR_NAME`/`GIT_AUTHOR_EMAIL` set to the human author. See
[github-bot-signing.md](github-bot-signing.md) for key generation and the
required `CHRONABOT_GPG_PRIVATE_KEY` / `CHRONABOT_GPG_PASSPHRASE` secrets.

Third-party actions that commit on their own behalf (Release Drafter, the
`lowlighter/metrics` renderer's own `committer_token` push) keep their own
identity, since GPG-signing an action's internal commit isn't something a
caller can attach after the fact.

## Releasing

1. Promote `canary` into `stable`. The Release workflow builds and verifies
   the signed APKs, and Release Drafter updates the draft release notes.
2. Publish the draft release (or push a `vX.Y.Z` tag). The tag triggers
   `release.yml`, which attaches the signed APKs, `SHA256SUMS.txt` and a build
   provenance attestation to the release.

## Secrets and variables

| Name | Used by |
| --- | --- |
| `ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD` | `release` environment (Release); repository level to sign Canary with the release key |
| `CHRONABOT_*_TOKEN`, `CHRONABOT_*_CHAT_ID` | Telegram bots, see `telegram-automation.md` |
| `CHRONABOT_GPG_PRIVATE_KEY`, `CHRONABOT_GPG_PASSPHRASE` | FebriCahyaa Bot signed commits (Crowdin, metrics) |
| `METRICS_TOKEN` | Metrics (classic PAT: `public_repo`, `read:user`) |
| `CROWDIN_PROJECT_ID`, `CROWDIN_PERSONAL_TOKEN` | Crowdin sync |

Branch promotion also needs **Settings → Actions → General → Allow GitHub
Actions to create and approve pull requests**.
