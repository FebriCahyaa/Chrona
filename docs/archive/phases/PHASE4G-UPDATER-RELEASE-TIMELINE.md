# Phase 4G — Updater Release Timeline

Status: DONE

## Scope

Phase 4G completes the selected Updater concept B: a release timeline surface that renders the latest stable GitHub release notes inside Chrona instead of forcing the user to leave the app for every review.

## Implementation

- Added `ChronaReleaseMarkdownParser` with a dependency-free Markdown subset for headings, bullets, task bullets, numbered items, paragraphs, and fenced code blocks.
- Added `ChronaReleaseTimeline` as a Material 3 bottom-sheet surface.
- Added published-at metadata formatting using the device timezone.
- Added separate `Release timeline` and `Open GitHub` actions in Settings.
- Kept the existing `AppUpdateRepository`, retry policy, WorkManager scheduler, and update persistence unchanged.

## Data model

The existing updater contract intentionally remains latest-release based. The UI therefore presents the latest release notes as a timeline of Markdown blocks; it does not claim to provide a historical multi-release feed.

## Safety / behavior

- No APK is installed automatically.
- External navigation remains an explicit user action.
- Empty or missing release notes produce a clear empty state.
- Malformed inline Markdown is simplified to readable plain text instead of rendered as executable content.
