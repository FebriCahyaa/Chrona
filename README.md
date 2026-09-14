# CLOCK APP

Modern Android clock dashboard built with Kotlin and Jetpack Compose.

## Android support

- Compile SDK: Android 17 / API 37
- Target SDK: Android 17 / API 37
- Minimum SDK: Android 8.0 / API 26
- Java: 17
- Android Gradle Plugin: 8.9.2

## Permissions

CLOCK APP currently requests **no runtime permissions**. A clock/dashboard does not need camera, location, contacts, storage, SMS, microphone, or notification access. Keeping the manifest permission-free improves privacy and reduces permission-related failures.

## Build with GitHub Actions

Push the repository to GitHub, then open **Actions**. The workflow installs Android 17 SDK packages, verifies the project structure, builds the debug APK, and uploads it as an artifact.

## Local build

```bash
gradle --version
gradle assembleDebug
```

The APK is generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```
