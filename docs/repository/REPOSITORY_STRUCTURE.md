<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Repository Structure

```text
Chrona/
├── app/
│   └── src/
│       ├── main/java/com/febricahyaa/clockapp/
│       │   ├── alarm/
│       │   ├── core/
│       │   ├── data/
│       │   │   └── timezone/
│       │   ├── di/
│       │   ├── model/
│       │   ├── navigation/
│       │   ├── notification/
│       │   ├── time/
│       │   ├── timer/
│       │   ├── ui/
│       │   └── widget/
│       ├── main/res/
│       ├── test/
│       └── androidTest/
├── config/
│   └── dependency-licenses.toml
├── docs/
│   ├── architecture/
│   ├── audit/
│   ├── build/
│   ├── ci/
│   ├── design/
│   ├── legal/
│   ├── localization/
│   ├── operations/
│   ├── release/
│   ├── repository/
│   └── world-clock/
├── scripts/
│   ├── audit/
│   ├── ci/
│   ├── dev/
│   ├── localization/
│   ├── release/
│   ├── telegram/
│   └── world-clock/
├── third_party/licenses/
├── .github/
│   ├── actions/setup-android/
│   ├── ISSUE_TEMPLATE/
│   └── workflows/
└── gradle/
```

Generated files stay out of the repository. Historical phase handoffs remain under `docs/archive/phases/` and are not mixed with active tooling.

## Localization

Language coverage and expansion policy are documented in [`docs/localization/LANGUAGES.md`](../localization/LANGUAGES.md).
