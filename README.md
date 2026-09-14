# CLOCK APP

A clean Android clock dashboard built with Kotlin and Jetpack Compose.

## Project principles

- **Readable source:** small composables with one responsibility.
- **Predictable state:** UI state is owned at the screen level and passed down explicitly.
- **Minimal permissions:** the current feature set does not require runtime permissions.
- **CI-first workflow:** builds are designed to run through GitHub Actions.
- **Consistent naming:** PascalCase for composables, descriptive names for state and callbacks.

## Available commands

Enter a command in the command bar and submit it from the keyboard:

- `dark` — enable dark theme.
- `light` — enable light theme.
- `settings` — open appearance settings.
- `reset` — restore the default dashboard state.

## Build locally

```bash
gradle assembleDebug
```

The generated APK is located at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Commit message convention

Use a detailed, action-oriented commit message:

```text
feat(clock): add live dashboard and theme controls

- Add a realtime HH:mm:ss clock display.
- Add light/dark theme switching.
- Add command bar actions for common dashboard controls.
- Split the screen into focused composables for maintainability.
- Keep the current feature set permission-free.

Verification:
- gradle assembleDebug
```

Keep commits focused: one logical change per commit, with a summary, detailed bullet points, and verification notes.

## Layout and input behavior

- The dashboard respects system bars and display cutouts.
- The content scrolls on smaller screens.
- The layout adjusts when the on-screen keyboard appears.
- Commands can be executed with the keyboard action or visible button.
- The keyboard is dismissed after command execution.


## Architecture refactor

This revision separates the Android activity entry point from the Compose application UI:

- `MainActivity.kt` — Android activity entry point only.
- `ClockApp.kt` — Compose application and UI composition.
- `model/ClockSettings.kt` — small domain model prepared for future settings work.

No new user-facing feature is introduced in this refactor. The goal is to make the next command and settings changes safer and easier to review.

Suggested commit:

```text
refactor(architecture): separate dashboard UI and application entry point
```

## Command architecture

The command system is now separated into a small typed layer:

- `command/ClockCommand.kt` — supported command types.
- `command/CommandParser.kt` — converts raw text into a command.
- `command/CommandResult.kt` — maps commands to user-facing result text.

The dashboard UI is not yet wired to this layer. This step intentionally prepares the architecture before changing command behavior.

Suggested commit:

```text
refactor(command): separate command parsing from dashboard UI
```


## Command feedback

The command bar now uses `CommandParser` and `CommandResult` to interpret commands and display feedback without mixing parsing logic into the dashboard UI. `CommandBar` (in `ui/components/`) is mounted directly in `ClockApp.kt` and is the layer's first real caller.


## Clock format settings

The dashboard now supports switching between 24-hour and 12-hour clock display.
Commands:
- `12` / `12h` / `12-hour`
- `24` / `24h` / `24-hour`


## Build validation note
The settings implementation was corrected so the clock format state is passed through the UI and all command cases are exhaustive.


## Command parser fix

All command variants now explicitly implement `ClockCommand`, including `Unknown`.
The parser returns `ClockCommand` consistently from every `when` branch.


## Dashboard V2
- Redesigned premium home dashboard with gradient hero clock card.
- Added visual stat cards, focus action card, and status panel.
- Fixed ClockDisplay spacing to use Dp values.

## Review fix pass
- Fixed `HomeScreen.kt`: it was declared under the wrong package (`com.example.clockapp`) and imported `ClockDisplay` from a package that doesn't exist in this project, so the module could not compile.
- Fixed a duplicate `modifier` argument on the "Focus on the time" `Card`, which is a Kotlin compile error.
- `ClockDisplay` now accepts `lightContent` (used by the gradient hero card) instead of silently failing to resolve.
- `HomeScreen`'s `onOpenClock` is now actually wired from `ClockApp.kt`, so tapping the focus card navigates to `ClockScreen`.
- Mounted `CommandBar` (new, in `ui/components/`) in `ClockApp.kt` so the command layer documented above is finally reachable from the UI.
- `MainActivity.kt` trimmed back down to an activity entry point, per this file's own "Architecture refactor" section — it had accumulated a full screen's worth of dead imports left over from before that refactor.
- Added Material You dynamic color (`dynamicLightColorScheme` / `dynamicDarkColorScheme` on API 31+, with a static fallback) so theming actually reflects MD3 rather than the default purple scheme.
- Replaced manual `statusBarColor` / `navigationBarColor` forcing in `styles.xml` with `enableEdgeToEdge()`, which is the supported approach on the API levels this app targets and avoids a hardcoded black nav bar on a light theme.
- `ClockApp.kt` now holds theme/seconds state as `ClockSettings` instead of a duplicate, disconnected `isDarkTheme` boolean, so the model in `model/ClockSettings.kt` is no longer dead code.
