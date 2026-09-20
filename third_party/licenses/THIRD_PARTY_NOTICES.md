<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Third-Party Notices

Chrona contains and/or depends on third-party open-source software. These components are **not** relicensed under Chrona's proprietary license and remain governed by their upstream terms.

## Declared Android/runtime dependencies

The following direct dependencies are declared in `gradle/libs.versions.toml` and recorded in `config/dependency-licenses.toml`:

- AndroidX Core, Annotation, Activity, Lifecycle, DataStore, WorkManager — Apache License 2.0.
- Jetpack Compose UI, animation, runtime-saveable, tooling-preview, tooling, Material 3, Material Icons, and Material 3 Adaptive — Apache License 2.0.
- Android Navigation Compose — Apache License 2.0.
- Kotlin Standard Library / kotlin-test — Apache License 2.0.
- Retrofit / converter-gson — Apache License 2.0.
- desugar_jdk_libs — Apache License 2.0.
- AndroidX Test — Apache License 2.0.
- Google Play services Location (`com.google.android.gms:play-services-location:21.4.0`) — Apache License 2.0.
- AndroidX Location Button Compose (`androidx.core.locationbutton:locationbutton-compose:1.0.0-alpha01`) — Apache License 2.0.
- JUnit 4.13.2 — Eclipse Public License 1.0.

## Pinned transitive dependencies

Chrona explicitly constrains these artifacts for compatibility/security control:

| Artifact | Version | License | Upstream |
| --- | --- | --- | --- |
| `org.bitbucket.b_c:jose4j` | 0.9.6 | Apache-2.0 | https://bitbucket.org/b_c/jose4j |
| `org.jdom:jdom2` | 2.0.6.1 | JDOM License | https://www.jdom.org/ |
| `org.apache.httpcomponents:httpclient` | 4.5.13 | Apache-2.0 | https://hc.apache.org/httpcomponents-client-4.5.x/ |
| `org.apache.commons:commons-lang3` | 3.18.0 | Apache-2.0 | https://commons.apache.org/proper/commons-lang/ |
| `org.bouncycastle:bcpkix-jdk18on` | 1.84 | Bouncy Castle License | https://www.bouncycastle.org/licence.html |

## Time-zone data

Chrona does not vendor a frozen IANA time-zone database. The World Clock catalog uses Android ICU/runtime data and canonical IANA zone identifiers. The current IANA upstream reference for this repository documentation is **2026d**, released 2026-09-11. See https://www.iana.org/time-zones/releases/2026d.

## License text and attribution

When an upstream component requires retention of a copyright notice, license text, or acknowledgement, that notice must remain available in the repository or distributed legal/about surface as applicable.

This inventory is intentionally tied to the Gradle version catalog. Resolved transitive graphs can change; therefore CI also runs dependency review and should be treated as the second line of dependency/license validation.
