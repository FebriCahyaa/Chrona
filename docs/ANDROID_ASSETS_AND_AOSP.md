# Android assets and AOSP integration

Chrona uses a CI-controlled synchronization process for official Google Material Design Icons/Symbols metadata and selected assets. The full Android SDK and complete AOSP tree are not committed to Git because they are build-system/platform sources rather than application assets.

The maintenance workflow performs this order:

1. Install Android 17 toolchain.
2. Synchronize Material Design and Android integration metadata.
3. Verify licenses and upstream revision files.
4. Compile Kotlin.
5. Run lint.
6. Build the debug APK.
7. Commit and push only after all previous steps succeed.

The workflow does not push changes when compilation, lint, or packaging fails.
