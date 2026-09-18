# Chrona Phase 4A — Interactive Bento Onboarding

## Scope

Implement the previously selected **Interactive Bento (B)** onboarding concept while preserving the existing DataStore completion and notification-permission flow.

## UX changes

- Replace the linear icon/title/body onboarding layout with a responsive Bento-style composition.
- Keep a large interactive hero preview for each onboarding step.
- Add supporting Alarm and Timer Bento cards.
- Add visible step counter and tappable progress indicators.
- Add Back navigation with `BackHandler`.
- Add explicit `Skip onboarding` action on later pages.
- Animate page changes using the centralized Chrona spatial/micro motion tokens.
- Use current device/local and world-clock preview data without adding a new persistence dependency.

## Architecture boundary

- `OnboardingViewModel` and `OnboardingRepository` are unchanged.
- `ClockApp` remains responsible for completion persistence and notification permission orchestration.
- No navigation route is added.
- No feature ViewModel or scheduler behavior is changed.

## Interaction model

```text
page 1 ── Continue ──> page 2 ── Continue ──> page 3 ── Get started ──> app
  ▲                       ▲                       │
  └──────── Back ─────────┴──────── Back ────────┘

Hero preview: tap to toggle Interactive state.
Progress pills: tap any completed/available step to jump directly.
```

## Exit criteria

- Interactive Bento composition is the default first-launch experience.
- Existing onboarding completion semantics remain idempotent.
- Back navigation never exits the onboarding before the first page.
- No source files are deleted.
- Relevant pure navigation tests are included.
