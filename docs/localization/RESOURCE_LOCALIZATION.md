# Chrona resource localization policy

The default `res/values/strings.xml` is the authoritative keyset. The Indonesian resource set is kept synchronized with the default keyset; translated values are preserved where available and new values fall back to the default English copy until reviewed by a translator.

The Spanish, French, Japanese, Korean, and Brazilian Portuguese resource sets intentionally remain translation subsets. Android falls back to the default resource for keys that are not translated yet. Legacy keys are removed instead of being duplicated under deprecated names.

User-facing UI copy belongs in Android string resources. Technical identifiers, date/time format tokens used by non-UI formatters, timezone IDs, persistence keys, JNI method names/descriptors, and developer diagnostics remain source-level constants by design.
