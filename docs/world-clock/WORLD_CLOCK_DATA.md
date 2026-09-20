<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# World Clock Data Strategy

Chrona separates three different kinds of geographic/time information.

## 1. Timezone truth

Civil-time rules are owned by Android's installed runtime data:

- `java.time.ZoneId` provides the available IANA-compatible IDs.
- Android ICU canonicalizes IDs and exposes region information.
- `getTZDataVersion()` is available for diagnostics.

Chrona does not ship a second frozen timezone database or calculate DST from a hand-maintained UTC offset table.

## 2. City labels

`WorldClockCityCatalog` provides human-friendly labels/aliases for common locations. `TimeZoneCatalog` merges those curated labels with the complete user-facing timezone ID set exposed by the device.

A city name is presentation metadata. The stable saved key is the IANA `zoneId`.

## 3. Device location

Current Location is a separate runtime capability:

```text
permission
  ↓
FusedLocationProviderClient
  ↓ fallback
LocationManager
  ↓ fallback
recent last-known
  ↓
reverse geocoding
```

The resolved location is transient UI state. It is not persisted as a World Clock item.

## Search vs geocoding

The city search screen does not need a remote Places API to determine timezone offsets. Search results are generated from Android's timezone catalog and the stable IANA ID is used for actual clock calculation.

Reverse geocoding is used only to turn the device coordinate into a human-readable current city/country. This keeps timezone math deterministic and prevents a network API from becoming the authority for DST.

A remote Places/geocoding provider can be added later behind a repository interface if the product requires place discovery beyond timezone data; it is not silently introduced as a mandatory dependency in the current build.

## Offline map

The map is a compact deterministic vector dataset already shipped in `WorldClockMapData.kt`. It is a visualization aid, not the authoritative geographic database. Timezone calculations never depend on map geometry.
