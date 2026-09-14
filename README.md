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
