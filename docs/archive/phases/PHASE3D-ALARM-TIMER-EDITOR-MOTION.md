# Phase 3D — Alarm & Timer Editor Spatial Motion

## Scope

This phase moves the Alarm creation/editing experience from an AlertDialog into a Material 3 ModalBottomSheet and establishes a dedicated editor motion contract for Alarm and Timer.

## Changes

- Added `ChronaMotionKeys.ALARM_EDITOR`.
- Added `ChronaMotionKeys.TIMER_EDITOR`.
- Replaced the Alarm add dialog with a unified create/edit bottom-sheet editor.
- Added an Edit action to expanded alarm cards.
- Preserved AlarmItem fields, persistence-facing callbacks, ringtone picker, repeat-day selection and vibration behavior.
- Added nested Timer editor motion identity around duration/countdown content.
- Kept existing navigation routes unchanged.
- No source files were deleted.

## Design intent

The editor is now a spatial surface rather than a blocking dialog. Its content is vertically expandable, remains bounded on wider windows, and reuses the centralized Chrona motion contract.

## Validation

Static source checks were performed during packaging. Full Android/Gradle compilation is intentionally left to the repository CI environment because the local wrapper may require downloading Gradle/dependencies.
