# Chrona Full Roadmap

The project is organized into 6 phases. Phase boundaries are milestone boundaries, not mandatory release numbers.

## Phase 1 — Foundation & Engine
Status: CLOSED
- ChronaTimeEngine with monotonic time source.
- Coroutines Flow state model.
- Navigation and SharedTransition foundation.

## Phase 2 — State Integration
Status: CLOSED
- App-scoped time engine integration.
- Timer/Stopwatch ViewModel integration.
- Navigation migration and persistence wiring.

## Phase 3 — Spatial Motion
Status: CLOSED at 3H
- Dashboard motion foundation.
- Shared card/destination transitions.
- Alarm/Timer editor motion.
- Timer/Stopwatch state motion.
- World Clock detail motion.
- Back-stack and motion polish.

## Phase 4 — Expressive UI Reconstruction
Status: CLOSED
4A — Interactive Bento Onboarding: DONE
4B — Hero Clock + Floating Cards: DONE
4C — Stopwatch Classic Vertical: DONE
4D — Timer Circular Hero + Dynamic Scaling: DONE
4E — Alarm Expressive Sheet Editor: DONE
4F — Settings Adaptive Dashboard: DONE
4G — Updater Release Timeline: DONE

## Phase 5 — Time Engine & Runtime Hardening
Status: CLOSED
- 5A — Unified Time Engine Hardening: DONE
- 5B — Timer / Alarm Durability Hardening: DONE
- 5C — Runtime Lifecycle / Performance Hardening: DONE
- Unified time projection without fixed-delay UI loops.
- Lifecycle/resume correctness.
- Background/foreground reconciliation.
- Accessibility, error, and state-restoration hardening.
- CI validation and instrumentation coverage.

## Phase 6 — Release & Quality Gate
Status: CLOSED at 6C
- 6A — Static Release Gate: COMPLETE
- 6B — Runtime / Device QA: COMPLETE (automation; physical execution delegated to CI/device)
- 6C — Release Candidate: COMPLETE
- Full UI regression pass.
- Android 17/device compatibility verification.
- Performance/jank audit.
- Localization/content audit.
- Release signing/build verification.
- Final changelog, release notes, and artifact packaging.

### Phase completion rule
A phase closes when its planned scope is implemented, source-audited, documented, and packaged. Runtime/device findings are handled as targeted fixes without creating arbitrary extra sub-phases.
