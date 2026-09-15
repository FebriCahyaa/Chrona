# Chrona icon implementation

## Design concept: Chrona Orbital Dial

The Chrona icon is a compact clock face with a deep warm-black background and a warm peach hand pair. The dial is intentionally simple so OEM launcher masks can crop it cleanly. The foreground is vector-based and contains the visual mark only; the background carries depth/contrast.

Android adaptive icon layers are 108dp and should keep the critical logo inside the central safe zone. The outer area remains available for launcher masking and visual effects.

## Source resources

```text
app/src/main/res/
├── drawable/ic_launcher_background.xml
├── drawable/ic_launcher_foreground.xml
├── drawable/ic_launcher_monochrome.xml
├── mipmap-anydpi-v26/ic_launcher.xml
├── mipmap-anydpi-v26/ic_launcher_round.xml
├── mipmap-anydpi-v33/ic_launcher.xml
└── mipmap-anydpi-v33/ic_launcher_round.xml
```

The adaptive icon XML is deliberately explicit:

```xml
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
```

## What "live icon" can and cannot mean on Android

A normal third-party application cannot request that the system launcher repaint its installed app icon every second. The launcher owns icon rendering and may additionally apply device-specific masks and effects. An adaptive icon is therefore a resource definition, not a continuously running app-owned surface.

Chrona uses three practical surfaces instead:

1. **Live in-app analog clock** — `LiveAnalogClock.kt`. It updates once per second while the Activity is RESUMED and suspends when the UI is not visible.
2. **Home-screen widget** — `ChronaClockWidgetProvider.java` + `widget_chrona_clock.xml`. The widget uses `TextClock`, allowing Android to update the displayed time without Chrona waking itself every second.
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
