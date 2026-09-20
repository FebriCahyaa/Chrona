<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# World Clock UI Contract

The current World Clock UI is the repository source of truth. Historical phase documents and prototypes are not authoritative.

## Screen anatomy

```text
┌─────────────────────────────────────┐
│ Top app bar                          │
│                                     │
│ Hero clock                          │
│  • large time                       │
│  • 12h / 24h                        │
│  • digital / analog                 │
│  • city + country                   │
│  • day/night + UTC offset           │
│                                     │
│ Current Location                    │
│  • permission / loading / resolved  │
│  • precise / approximate state      │
│                                     │
│ Saved                               │
│  ┌────────────┐ ┌────────────┐      │
│  │ Favorite 1 │ │ Favorite 2 │      │
│  ├────────────┤ ├────────────┤      │
│  │ Favorite 3 │ │ Favorite 4 │      │
│  └────────────┘ └────────────┘      │
│                                     │
│ Additional cities                  │
│  ┌───────────────────────────────┐  │
│  │ City                         │  │
│  │ Time · day/night · UTC       │  │
│  └───────────────────────────────┘  │
│                                     │
│ World Map                           │
│                                     │
│        Search  Clock  Map           │
└─────────────────────────────────────┘
```

## Motion

The hero participates in the same scroll context as the city list. Collapse progress is derived from `LazyListState` instead of maintaining an independent scroll controller.

The collapse transform is continuous: scale, translation and opacity change with scroll progress and are lightly spring-smoothed. Once the user moves beyond the hero, a compact clock header becomes available to return to the top.

Navigation is state-based: Material 3 owns the selected `NavigationBarItem` indicator. The screen does not use a custom moving pill.

## Current Location

Current Location is always present so an empty saved-city state does not create a dead-end screen.

| State | UI |
| --- | --- |
| No permission | Android/Jetpack Location Button |
| Requesting | Progress + explanatory text |
| Fresh | City/country + precision badge + refresh |
| Unavailable | Retry + location settings |
| Permission denied | Transient snackbar with settings action |

Location is foreground-only. It is not written into `WorldClockRepository`.

## Seconds modes

The settings surface exposes five modes:

- `STACKED`: vertical secondary seconds.
- `INLINE`: one-line `HH:MM:SS`.
- `FADING_SCROLL`: vertically animated current second with blurred neighbors.
- `MINIMAL`: quiet secondary seconds.
- `CIRCULAR`: Material 3 `CircularWavyProgressIndicator`.

The selected mode is stored in `ClockSettings` and is consumed by the hero clock. City cards remain compact and use the stable textual formatter rather than running five different seconds renderers across the grid.

## World Map

The map is fixed and offline. The existing repository geometry remains the deterministic visualization source.

Allowed interaction:

- no drag
- no pan
- no pinch zoom
- no manual meridian movement

The device location creates a projected marker and highlights the map feature containing that point when the simplified geometry has a matching region.

## Accessibility

Interactive controls expose content descriptions. Text sizing and colors are sourced from Material 3 typography and color roles. The map publishes a state description rather than requiring users to interpret geometry alone.
