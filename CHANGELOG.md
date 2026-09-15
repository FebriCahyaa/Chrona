# Changelog

## 0.3.0 — Chrona UI / Native Foundation

### Added
- Material 3 Expressive-inspired live analog clock on the main page.
- Lifecycle-aware 1 Hz clock ticker aligned to second boundaries.
- C++20 native clock-angle and timer math through a Java JNI boundary.
- Pure Java fallback clock math and JVM tests.
- Adaptive icon with explicit background, foreground and Android 13+ monochrome layers.
- Home-screen clock widget using `TextClock` to avoid a per-second app wake-up loop.
- GitHub Actions quality, build, dependency review, dependency submission and CodeQL workflows.
- Dependabot configuration plus issue templates.

### Changed
- Build upgraded to AGP 9.4.0, Gradle 9.6.1, compile/target SDK 37 and NDK r30 LTS.
- Digital clock and shared time state now suspend when the Activity is not RESUMED.
- Rust timing prototype replaced by the requested Java/C++ architecture.

### Design intent
- Preserve the Chrona identity: warm accent, dark depth, glass-aware surfaces and strong time hierarchy.
- Keep the launcher icon static because standard Android launchers do not expose a continuous per-second app-icon rendering API.
