<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Chrona icon implementation

## Design concept: Chrona Indigo Dial

The Chrona icon is a minimal analog clock: a thin outlined dial, four cardinal tick marks, and hour/minute hands resting at 10:10 (the balanced icon-clock convention) on a diagonal indigo → violet gradient. The second hand and centre dot use a single coral accent (`#FF6B4A`). The foreground is vector-based and contains the visual mark only; the background carries depth/contrast.

| Element | Colour |
| --- | --- |
| Background gradient | `#1C1B2E` → `#332B5C` → `#5B4BDB` (diagonal) |
| Dial, ticks | `#F2F0FF` |
| Hour / minute hands | `#FFFFFF` |
| Second hand, centre dot | `#FF6B4A` |
| Monochrome layer | solid black mask, tinted by the system |

Android adaptive icon layers are 108dp and should keep the critical logo inside the central safe zone. The outer area remains available for launcher masking and visual effects.

## Source resources

```text
app/src/main/res/
├── drawable/ic_chrona_background.xml
├── drawable/ic_chrona_foreground.xml
├── drawable/ic_chrona_monochrome.xml
├── drawable/ic_stat_chrona.xml            # 24dp notification small icon
├── mipmap-anydpi-v26/ic_launcher.xml
├── mipmap-anydpi-v26/ic_launcher_round.xml
└── mipmap-{m,h,xh,xxh,xxxh}dpi/ic_launcher{,_round}.png   # legacy fallbacks
```

The adaptive icon XML is deliberately explicit:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_chrona_background" />
    <foreground android:drawable="@drawable/ic_chrona_foreground" />
    <monochrome android:drawable="@drawable/ic_chrona_monochrome" />
</adaptive-icon>
```

Because `minSdk` is 26, a single `mipmap-anydpi-v26` definition is enough. The `<monochrome>` element (Themed Icons, Android 13+) is ignored by Android 8–12, so no separate `-v33` resource set is required.

The three hands in the foreground/monochrome layers are named groups (`hourHand`, `minuteHand`, `secondHand`) pivoting on the dial centre `(54,54)`. They are reserved as `AnimatedVectorDrawable` targets for a future splash animation; the launcher icon itself stays static.

`ic_stat_chrona.xml` is the notification small icon used by alarm and timer notifications. Android renders small icons from the alpha channel only, so it is a simplified white silhouette (ring, two hands, centre dot) instead of the full-colour launcher artwork.

## What "live icon" can and cannot mean on Android

A normal third-party application cannot request that the system launcher repaint its installed app icon every second. The launcher owns icon rendering and may additionally apply device-specific masks and effects. An adaptive icon is therefore a resource definition, not a continuously running app-owned surface.

Chrona uses three practical surfaces instead:

1. **Live in-app analog clock** — `LiveAnalogClock.kt`. It updates once per second while the Activity is RESUMED and suspends when the UI is not visible.
2. **Home-screen widget** — `ChronaClockWidgetProvider.kt` + `widget_chrona_clock.xml`. The widget uses `TextClock`, allowing Android to update the displayed time without Chrona waking itself every second.
3. **Static adaptive app icon** — the three-layer icon above, with a monochrome variant for themed icon systems.

## Custom launcher / OEM implementation

A launcher author can make a genuinely live analog app icon because the launcher controls the rendering surface. A robust implementation would:

- resolve the app's adaptive icon layers once;
- cache the vector paths;
- render only the three hand paths on the launcher UI thread or renderer;
- derive angles from a monotonic wall-clock snapshot;
- refresh at 1Hz, or interpolate the second hand only when the launcher is visible;
- pause rendering when the launcher process is not interactive;
- avoid waking the Android app process merely to rotate the icon.

That approach belongs to the launcher, not to the Chrona APK itself.

## Battery decision

Do **not** implement a rotating launcher icon by scheduling an exact alarm every second or by running a permanent foreground service. Android's widget guidance specifically notes that frequent widget updates can be computationally expensive and should be optimized. A launcher-owned renderer is fundamentally more efficient because it is already responsible for drawing the home screen.
