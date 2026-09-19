<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# World Clock Data Strategy

Chrona does **not** ship a hand-maintained list of 20–30 cities as its authoritative database. The user-facing catalog is generated at runtime from Android ICU/Java time-zone IDs and canonicalized through ICU.

## Runtime source

`TimeZoneCatalog` uses:

- `ZoneId.getAvailableZoneIds()` for the installed timezone ID set.
- `android.icu.util.TimeZone.getCanonicalID()` for canonicalization.
- `android.icu.util.TimeZone.getRegion()` for country/region mapping.
- `android.icu.util.TimeZone.getTZDataVersion()` to expose the device timezone data version for diagnostics.

This lets Chrona follow timezone-rule updates shipped with Android instead of requiring a new app database for every timezone-rule change.

Android exposes these ICU timezone APIs on API 24+, and Chrona's minimum SDK is API 26. The IANA timezone database remains the upstream source for civil-time rules; the current IANA release at the time this document was updated is 2026d, released 2026-09-11.

## Search behavior

The catalog searches by:

- city/zone label,
- localized country name,
- ISO region code,
- IANA zone ID.

Obscure but valid geographic zones remain discoverable. Fixed-offset `Etc/*` zones are intentionally excluded from the normal city catalog because they are not city/region locations.

## Persistence

World Clock favorites are stored by stable IANA zone ID, not by localized city name. A compatibility migration still recognizes older city-name favorites and normalizes them to zone IDs on first load.

## Upstream provenance

The IANA Time Zone Database release referenced for the current repository audit is **2026d**, published 2026-09-11. The source distribution is available at https://www.iana.org/time-zones/releases/2026d. Chrona intentionally does not copy that database into the APK.
