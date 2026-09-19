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
│   └── telegram/
├── third_party/licenses/
├── .github/
│   ├── actions/setup-android/
│   ├── ISSUE_TEMPLATE/
│   └── workflows/
└── gradle/
```

Generated files and one-off patch scripts stay out of the repository. Active documentation records current contracts and operational guidance only.

## Localization

Language coverage and expansion policy are documented in [`docs/localization/LANGUAGES.md`](../localization/LANGUAGES.md).
