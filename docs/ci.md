# CI and Branching

## Branches

| Branch | Purpose | Workflow | Output |
| --- | --- | --- | --- |
| `main` | Integration branch; every change lands here first | Build Debug | Debug APKs (artifact) |
| `dev` | Development line promoted from `main` | Build Dev | Dev APKs (artifact) |
| `canary` | Pre-release line promoted from `dev` | Build Canary | Canary APKs + GitHub pre-release |
| `stable` | Release line promoted from `canary` | Release | Signed APKs; GitHub Release on `v*` tags |

Changes flow `main → dev → canary → stable`. Run **Branch Promotion**
(Actions → Branch Promotion → Run workflow) to open the promotion pull request,
and merge it with a merge commit so the branches keep a shared history.

## Workflows

| File | Trigger | Jobs |
| --- | --- | --- |
| `build-debug.yml` | push `main`, pull requests | quality + Debug APK, SDK channel 1 (beta) |
| `build-dev.yml` | push `dev` | quality + Dev APK, SDK channel 2 (dev) |
| `build-canary.yml` | push `canary` | quality + Canary APK, SDK channel 3 (canary), pre-release via gh-release |
| `android-build.yml` | called by the three above | Telegram start, quality, build, Telegram report |
| `release.yml` | push `stable`, `v*` tags | signed Release APK, SDK channel 0 (stable), attestation, gh-release |
| `release-drafter.yml` | push `stable`, pull requests | next-release draft, PR autolabels |
| `security.yml` | push, pull requests, weekly | CodeQL, TruffleHog, dependency review, dependency graph |
| `lint.yml` | push, pull requests | Super-Linter: actionlint, YAML, Markdown, ShellCheck, Ruff |
| `metrics.yml` | daily | lowlighter/metrics → `metrics.svg` on the `metrics` branch |
| `branch-promotion.yml` | manual | promotion pull request between release lines |
| `automation.yml` | PRs, issues, weekly | Telegram notifications, labels, changelog, Crowdin |

All Android jobs use the composite action `.github/actions/android-toolchain`
(JDK 17, SDK for a channel, Gradle). Every build line can be run manually with
optional `platform` / `build-tools` inputs to pin a specific package.

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
| `METRICS_TOKEN` | Metrics (classic PAT: `public_repo`, `read:user`) |
| `CROWDIN_PROJECT_ID`, `CROWDIN_PERSONAL_TOKEN` | Crowdin sync |
| `CHRONABOT_GPG_PRIVATE_KEY`, `CHRONABOT_GPG_PASSPHRASE` | signed bot commits |

Branch Promotion also needs **Settings → Actions → General → Allow GitHub
Actions to create and approve pull requests**.
