# Phase 5B — Timer / Alarm Durability Hardening

Phase 5B hardens the boundary between persisted state and Android background side effects.

## Timer durability

- Introduce `TimerDurabilityPolicy` as a pure recovery state machine.
- Restore running timers from their persisted epoch deadline instead of a fixed counter.
- Persist `completionPending = true` before starting any completion side effect.
- Keep completion pending if foreground-service startup fails.
- Clear the pending completion only after a foreground service or fallback notification has been successfully established.
- Reuse the same recovery policy in `TimerViewModel`, `TimerReceiver`, and `BootReceiver`.
- Commit timer state to persistence before scheduling/cancelling the corresponding AlarmManager operation, reducing the stale-side-effect window.

## Alarm durability

- Add an explicit snooze marker to alarm intents.
- Reject stale normal alarm broadcasts when the alarm was deleted or disabled after scheduling.
- Preserve explicit snooze behavior even when the original one-shot alarm has already been disabled.
- Re-check the alarm state inside `AlarmService` to close the receiver/service race window.
- Reconcile alarm state before starting ringtone/vibration.
- Commit the alarm trigger transition from notification actions as well, so dismiss/snooze remains durable if the foreground service is stopped early.

## Scope

No new persistence backend, navigation route, UI surface, or dependency is introduced in this phase.
