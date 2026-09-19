# Chrona Phase 2 — Clock Experience Revamp

## Scope

This phase refines the existing Home clock experience without changing Chrona's timing engine, persistence contract, navigation architecture, or Digital/Analog preference model.

## Changed

- Strengthened the Home clock hero hierarchy with a clearer day/night status row.
- Added a compact Material 3 tonal context surface for date, local-time label, and UTC offset.
- Added an explicit Digital/Analog selector directly beneath the hero clock so the persisted preference is discoverable without opening Settings.
- Kept the existing top-right quick toggle and made both entry points use the same `onDisplayModeChange` state flow.
- Increased digital hero typography to 80sp and retained tabular numerals for stable digit widths.
- Promoted AM/PM and optional seconds into compact tonal chips.
- Kept the smooth analog second hand and existing analog renderer intact.
- Reused the existing localized Digital/Analog strings from Settings instead of introducing duplicate labels.

## Added

- `ClockModeSwitcher`.
- `ClockModeChoice`.
- `formatUtcOffset()`.
- Explicit date/local-time/UTC context surface.

## Removed

- No persisted settings field.
- No timing engine or time source.
- No navigation route.
- No Digital/Analog renderer.

## Not Removed

- `ClockDisplayMode` persistence.
- Digital as the first-launch default.
- Settings segmented clock-style control.
- `rememberSmoothZonedNow()` and the analog second-hand animation.
- 12/24-hour formatting and show-seconds preference.
- Existing Material 3 / Glass component system.

## Apply

```bash
bash scripts/apply-phase2-clock-experience.sh
bash scripts/verify-phase2-clock-experience.sh
```

The script is intentionally source-anchored and stops when the expected `ClockHero` / `TimeActionCard` structure is absent, reducing the risk of silently patching an incompatible repository revision.

## Verification note

Repository source structure can be checked locally after applying. Full Gradle build/test execution still depends on the user's Android/Gradle environment and network availability.
