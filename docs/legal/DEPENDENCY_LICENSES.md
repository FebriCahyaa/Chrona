<!-- Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved. -->
# Chrona Dependency License Register

This register documents the third-party artifacts explicitly declared in Chrona's Gradle version catalog or deliberately pinned as transitive compatibility/security constraints. It is an engineering/legal inventory, not a replacement for upstream license texts or notices.

## Direct dependencies

Chrona's AndroidX, Jetpack Compose, Material 3, Navigation, Retrofit, desugaring, and Kotlin libraries use the Apache License 2.0. JUnit 4 uses EPL-1.0.

| Artifact family | License | Upstream |
| --- | --- | --- |
| AndroidX / Jetpack Compose / Material 3 / Adaptive / Navigation | Apache-2.0 | developer.android.com |
| Google Sans Flex font | OFL-1.1 | fonts.google.com / github.com/googlefonts/googlesans-flex |
| Kotlin / kotlin-test | Apache-2.0 | kotlinlang.org / github.com/JetBrains/kotlin |
| Retrofit / converter-gson | Apache-2.0 | github.com/square/retrofit |
| desugar_jdk_libs | Apache-2.0 | github.com/google/desugar_jdk_libs |
| androidx.test.ext:junit | Apache-2.0 | developer.android.com |
| JUnit 4 | EPL-1.0 | junit.org |

## Pinned transitive artifacts

The repository constrains several transitive artifacts to known versions for security/compatibility reasons. They remain governed by their upstream licenses.

| Artifact | Version | License | Evidence |
| --- | --- | --- | --- |
| org.bitbucket.b_c:jose4j | 0.9.6 | Apache-2.0 | Upstream/Maven Central metadata |
| org.jdom:jdom2 | 2.0.6.1 | JDOM License | Upstream/Maven Central metadata |
| org.apache.httpcomponents:httpclient | 4.5.13 | Apache-2.0 | Upstream/Maven Central metadata |
| org.apache.commons:commons-lang3 | 3.18.0 | Apache-2.0 | Apache Commons upstream metadata |
| org.bouncycastle:bcpkix-jdk18on | 1.84 | Bouncy Castle License | Upstream/Maven Central metadata |

The checked-in machine-readable register is [`config/dependency-licenses.toml`](../../config/dependency-licenses.toml). CI verifies that every artifact declared by the version catalog has a corresponding record. GitHub dependency review additionally evaluates dependency changes and license/vulnerability information on pull requests.

## Scope and limitations

Gradle's resolved dependency graph can change when transitive metadata changes. Chrona therefore does not claim that this static register alone is exhaustive for every future graph. Pull requests must pass dependency review, and release CI preserves the resolved build metadata used for publication.
