# CLOCK APP

Modern Android clock dashboard built with Kotlin and Jetpack Compose.

## Stable build profile

- `compileSdk`: 36
- `targetSdk`: 36
- `minSdk`: 26
- Java/JDK: 17
- Android Gradle Plugin: 8.9.2
- Gradle: 8.11.1
- GitHub Actions: Ubuntu latest

## Permissions

The current app requests no runtime permissions. The manifest intentionally does not include camera, microphone, location, contacts, storage, SMS, Bluetooth, notification, or internet permissions.

Add permissions only when the related feature is implemented and actually requires them.

## GitHub Actions

The workflow verifies the project, installs Android SDK 36, builds a debug APK, and uploads the APK as an artifact.

## Manual build

```bash
gradle assembleDebug
```

APK output:

```text
app/build/outputs/apk/debug/app-debug.apk
```
