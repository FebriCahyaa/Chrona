# World Clock UI Revamp

## Baseline

Built against `main` at `dea3a3443714626c7f84ed85a5f0ea79e42a426c`.

## Design intent

The World Clock destination is rebuilt around the supplied HTML world-map reference and the requested asymmetrical dashboard composition:

- local device time as the first visual anchor;
- a spotlight area with London, Paris, New York, and Los Angeles when those saved zones exist;
- a responsive 2-column city-card treatment on compact layouts and an asymmetrical hero + city-grid composition on wider layouts;
- an offline vector world map with a draggable UTC meridian, probe point, live continent highlight, and moving UTC tag;
- the existing persistent World Clock state, favorites, region filters, search flow, and detail navigation remain intact.

## Map source policy

The supplied HTML reference attempts multiple remote geometry sources and then falls back to continent-level polygon geometry. Chrona does not introduce a WebView or CDN dependency for the Android UI. The fallback polygon geometry from the supplied source is embedded into `WorldClockMapData.kt`, preserving offline rendering and the source's simplified highlighting behavior.

## Interaction

Dragging across the map changes the UTC offset in one-hour steps and moves the probe vertically. The continent under the probe is highlighted. A second highlight state is applied to land that intersects the selected meridian, matching the visual intent of the supplied reference.

## Commit handoff

See `WORLD_CLOCK_REVAMP_COMMIT_MESSAGE.txt` for the complete conventional commit message plus Changed / Added / Removed / Not Removed / Verification / Scope sections.

## Validation scope

Validated offline in the working container:

- map geometry extracted from the supplied HTML fallback: 7 features, 981 source points;
- Android resource insertion script passes Python syntax validation;
- Kotlin source brace/parenthesis balance and required imports checked statically;
- XML string resources remain well-formed after scripted insertion in a temporary validation copy.

A full Gradle build was not claimed because the environment cannot reach Gradle distribution hosts and the repository is not available as a direct clone from this container.
