# Chrona Data Storage

| Layer | Responsibility | Authority |
|---|---|---|
| Room | alarms, world-clock entries, alarm history | structured user state |
| Preferences DataStore | settings, timer, stopwatch, onboarding, update cache | asynchronous preferences/state |
| Android Keystore + encrypted DataStore payload | optional secret/auth token storage | device-bound secrets |
| SharedPreferences | migration adapter only | legacy input, not runtime state |

`StorageMigrationCoordinator` performs the one-time migration from the previous
SharedPreferences stores. After successful migration, legacy files are cleared.

Repositories expose `Flow` for observation and suspend functions for mutations. UI observes
local state first. WorkManager/network tasks are synchronization/update inputs and never a
prerequisite for rendering persisted state.
