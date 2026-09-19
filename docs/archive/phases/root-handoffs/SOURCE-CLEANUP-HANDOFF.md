# Chrona Source Cleanup Handoff

## Scope

This cleanup pass follows the Phase 6C release-candidate baseline and normalizes user-facing resources, source-language boundaries, and project artifact layout.

## Resource policy

User-facing copy is resolved through Android resources. The default English resource set is the canonical keyset. Indonesian resources are synchronized as the maintained primary translation. Existing non-Indonesian locale translations are preserved as reviewed subsets rather than fabricating translations for newly introduced keys; Android fallback supplies English for missing translations.

Technical literals remain in source when they are not user-facing, including JNI method names/signatures, native library names, date/time format patterns, animation/debug labels, persistence keys, intent extras, and punctuation/separator glyphs.

## Java / C++ policy

Production Java is intentionally limited to the JNI bridge. Pure time math and the widget provider were migrated to Kotlin. C++ remains for native time/math operations and `RegisterNatives` ABI metadata. JNI class/method/signature literals are not localization resources because they are runtime linkage contracts.

## Phase artifacts

Completed Phase 3-6 artifacts were moved from repository root into `docs/archive/phases/` so the active root contains product source, build tooling, and high-level project documentation only.

## Known validation limitation

Full Android Gradle compilation remains dependent on the configured Gradle 9.7.1 distribution being available to the environment. Device-level verification remains part of CI/device execution.
