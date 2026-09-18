# Phase 4E Changelog

## Added

- Full-height expressive Alarm editor sheet.
- Large selected-time preview.
- Sticky bottom action bar.
- Scrollable Schedule and Alert behavior sections.
- Alarm enabled control inside the editor.
- IME/navigation-bar safe sheet layout.

## Changed

- AlarmScreen editor now uses the Material 3 sheet as the single Create/Edit surface.
- Unsaved editor state is isolated until Save/Create is committed.
- Roadmap marks 4E complete.

## Fixed

- Removed literal `\\n` sequences accidentally embedded in the previous AlarmScreen source artifact.
- Preserved compatibility for callers that do not supply the legacy `onEdit` callback.

## Removed

- Nothing.
