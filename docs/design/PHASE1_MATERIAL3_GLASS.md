<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Phase 1 — Material 3 + Glass UI Foundation

## Baseline

This phase is applied to the synchronized `Chrona-main (1).zip` baseline whose GitHub `main` source was verified before modification.

## Implemented

- `ChronaScaffold` uses Material 3 `LargeFlexibleTopAppBar` with one remembered `TopAppBarState`.
- Title and subtitle are first-class M3 top-app-bar slots and collapse together with the navigation icon and actions through one `TopAppBarScrollBehavior`.
- The custom clickable Bento card now uses the Material 3 ripple indication while retaining its press-scale feedback.
- `GLASS` is exposed directly in Settings and propagated through `ChronaTheme` composition locals so theme-aware UI primitives do not need a second global state source.
- `AppThemeMode.GLASS` is wired into `ChronaTheme`.
- `ThemeEngine.glassScheme()` supplies a Material 3-compatible translucent surface palette.
- `ChronaCard(glass = true)` now selects the GLASS rendering mode instead of masquerading as Material You.
- `HybridBentoCard` has a restrained glass surface, border, highlight and low-elevation treatment.
- Shared Phase 1 glass constants live in `ChronaGlassTokens`.

## Intentionally deferred

AGSL backdrop refraction, dynamic blur and shader-driven distortion are not part of Phase 1. The foundation is deliberately based on M3 surfaces and the existing ambient backdrop so rendering cost stays bounded while the hierarchy, interaction and scrolling model are stabilized.

## Scope boundary

World Clock, Timer, Stopwatch and Settings layout redesigns are subsequent phases.
