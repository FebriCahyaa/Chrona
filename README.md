# Chrona

[![Build](https://github.com/FebriCahyaa/Chrona/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/FebriCahyaa/Chrona/actions/workflows/build.yml)
[![Release](https://github.com/FebriCahyaa/Chrona/actions/workflows/release.yml/badge.svg)](https://github.com/FebriCahyaa/Chrona/actions/workflows/release.yml)
[![Checks](https://github.com/FebriCahyaa/Chrona/actions/workflows/checks.yml/badge.svg?branch=main)](https://github.com/FebriCahyaa/Chrona/actions/workflows/checks.yml)

Chrona is an Android clock application derived from the AOSP DeskClock
lineage and maintained as an independently engineered project by FebriCahyaa.

## Release lines

| Branch | Build | Android SDK channel | Download |
| --- | --- | --- | --- |
| `stable` | Release (signed) | stable (`0`) | [Latest release](https://github.com/FebriCahyaa/Chrona/releases/latest) |
| `canary` | Canary | canary (`3`) | [Canary pre-releases](https://github.com/FebriCahyaa/Chrona/releases) |
| `dev` | Dev | dev (`2`) | Actions artifacts |
| `main` | Debug | beta (`1`) | Actions artifacts |

`main` is the integration branch for all commits. Changes are promoted
`main → dev → canary → stable`; see [CI and branching](docs/ci.md).

## Build

```sh
./gradlew assembleDebug
./gradlew lintDebug testDebugUnitTest
```

Gradle 9.7.1, Android Gradle Plugin 9.4.1, Kotlin 2.4.10 and JDK 17. CI
installs the newest Android platform and build-tools of each build line's SDK
channel. Details: [docs/build.md](docs/build.md).

`Android.bp` is kept for AOSP/Soong builds and reads the same sources.

## Repository metrics

![Chrona metrics](https://raw.githubusercontent.com/FebriCahyaa/Chrona/metrics/metrics.svg)

## Documentation

- [Build, toolchain and layout](docs/build.md)
- [CI, branching and releasing](docs/ci.md)
- [Release signing](docs/release-signing.md)
- [Telegram automation](docs/telegram-automation.md)
- [Contributing](.github/CONTRIBUTING.md) · [Security policy](.github/SECURITY.md) · [Code of conduct](.github/CODE_OF_CONDUCT.md)

## License

Chrona is distributed under the Apache License 2.0 (see [LICENSE](LICENSE)
and [NOTICE](NOTICE)). Copyright and third-party attribution are recorded in
[docs/legal](docs/legal/COPYRIGHT.md).
