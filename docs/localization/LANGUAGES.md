<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Localization Languages

Chrona treats language, locale, and country as different concepts. The Android default resource set (`values/strings.xml`) is the complete source keyset; translations are maintained externally through Crowdin and are synchronized back into Android locale-qualified `values-*` directories.

## Initial production language set

- English (`en`) — source language.
- Indonesian (`id`) — primary translated language.
- Spanish (`es`).
- French (`fr`).
- German (`de`).
- Portuguese / Brazil (`pt-BR`).
- Portuguese / Portugal (`pt-PT`) where Crowdin/Android mapping requires a region-specific resource.
- Japanese (`ja`).
- Korean (`ko`).
- Chinese Simplified (`zh-rCN`).
- Chinese Traditional (`zh-rTW`).
- Arabic (`ar`).
- Hindi (`hi`).
- Bengali (`bn`).
- Vietnamese (`vi`).
- Thai (`th`).
- Turkish (`tr`).
- Italian (`it`).
- Dutch (`nl`).
- Polish (`pl`).
- Ukrainian (`uk`).
- Russian (`ru`).

## Expansion policy

Crowdin may host additional languages and regional variants without requiring a matching source directory to be committed by hand. The application always retains the complete English default file so an untranslated key falls back safely to English. Android resolves the best matching localized resource at runtime.

Do not create one resource directory per country. Many countries share a language, while some languages require multiple regional variants. Add region-specific resources only when product copy or terminology genuinely differs.

## QA

Localization CI checks key coverage against the default source file. Before release, QA should exercise the app under representative BCP-47 locales, including one RTL locale (`ar`), one CJK locale (`ja` or `zh-CN`), one long-text locale (`de`), and the primary locale (`id`).

See `crowdin.yml` and `docs/localization/LOCALIZATION.md` for synchronization details.
