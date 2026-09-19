# Chrona Phase 3G — World Clock Detail Spatial Motion

## Scope

Add a real World Clock detail destination and connect each saved World Clock card to that destination using the same stable shared-bounds identity.

## Changes

- Add `AppDestination.WORLD_DETAIL`.
- Add an encoded query route: `world/detail?zoneId={zoneId}`.
- Add `ChronaNavigationActions.openWorldClockDetail(zoneId)`.
- Register the parameterized detail destination inside `ChronaRootNavigation`.
- Pass the active `NavBackStackEntry` into destination content so route arguments remain local to the destination.
- Add `WorldClockDetailScreen` with a live clock, date, UTC offset, local delta, timezone identity, and favorite control.
- Make World Clock list cards clickable and attach `worldClockCard(item.id)` to the source card.
- Attach the same key to the detail hero card.
- Fix the previous empty-state `item.id` out-of-scope reference in `WorldClockScreen`.

## Motion contract

The list source and detail destination both use:

`ChronaMotionKeys.worldClockCard(item.id)`

The dashboard-to-World Clock root contract remains:

`ChronaMotionKeys.DASHBOARD_WORLD_CLOCK`

This gives Chrona a two-stage motion hierarchy:

`Dashboard → World Clock → City Detail`

## Route safety

Zone IDs such as `Asia/Jakarta` contain `/`. The detail destination therefore uses a query argument and URL encoding instead of embedding the raw Zone ID inside a slash-delimited path.

## Deletions

None.
