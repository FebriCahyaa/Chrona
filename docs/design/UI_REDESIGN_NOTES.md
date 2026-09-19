<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# Chrona UI system redesign

This revision rebuilds the Chrona presentation layer around one visual system:

- One destination is composed at a time. The previous `AnimatedContent` navigation stack was removed to prevent old screens from remaining under the new screen.
- Glasses mode is an intentionally dark, translucent presentation with accent glow, fine borders, layered surfaces and stronger depth.
- Light and Dark keep the same component geometry while changing the material palette.
- Cards, pills, icon buttons, section labels and bottom navigation now share the same radius, border, spacing and typography tokens.
- Home follows the reference hierarchy: brand header, location/time context, large local-time hero, utility stats, and quick-access tools.
- Timer and Stopwatch use a focused hero surface instead of drawing the control directly over the root background.
- World Clock and Alarm use the same card language as Home and Settings.
- Settings preview and theme selectors were rebuilt to match the same component language.

