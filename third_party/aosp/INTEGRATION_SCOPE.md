<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->

# Integration scope

Chrona uses the installed Android SDK and resolved AndroidX/Compose artifacts.

The Android SDK is managed in CI through the official Android CLI. The complete AOSP tree is not copied into the application repository. Only source-level
patterns compatible with the current Android API, AndroidX, Compose, and Material 3 APIs
should be integrated into Chrona.
