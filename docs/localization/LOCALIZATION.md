<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Localization

Chrona uses Android resource localization as the runtime boundary and Crowdin as the translation management system.

## Source of truth

The English source file is:

`app/src/main/res/values/strings.xml`

Translations live under `app/src/main/res/values-<android-locale>/strings.xml`.

The Indonesian translation uses `values-id`, matching Android resource locale conventions.

## Automation

The `sync-source.yml` workflow uploads the English source, downloads approved translations, and creates a pull request for localized resource changes. Crowdin's current GitHub Action v3 uses Crowdin CLI 5 and supports this upload/download/PR workflow.

Required repository secrets:

- `CROWDIN_PROJECT_ID`
- `CROWDIN_PERSONAL_TOKEN`

Do not commit either value.

## Language strategy

“Every country” is not modeled as one language per country. Chrona targets languages/locales, and one language can cover multiple countries while one country can contain multiple locales. Crowdin should be used to expand the supported locale set rather than generating hundreds of empty Android resource folders.

## Quality gate

`scripts/localization/audit-resources.py` checks for stale keys and reports translation coverage. It does not reject intentionally partial translations unless `--strict` is explicitly requested.
