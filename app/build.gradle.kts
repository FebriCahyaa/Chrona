import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins { id("com.android.application") }

fun env(name: String): String? =
    providers.environmentVariable(name).orNull?.trim()?.takeIf { it.isNotEmpty() }

// Toolchain selection. CI resolves these from the Android SDK channel that
// matches the build (see scripts/ci/android_sdk.py); local builds fall back to
// the stable defaults below.
//   CHRONA_COMPILE_SDK  "37.2" (API 37, minor 2), "37", or a preview codename
//   CHRONA_BUILD_TOOLS  e.g. "37.0.0"; unset lets AGP pick its default
val compileSdkSpec = env("CHRONA_COMPILE_SDK") ?: "37.2"
val buildToolsOverride = env("CHRONA_BUILD_TOOLS")

// Versioning. CI injects a monotonically increasing code and a channel label.
val chronaVersionCode = env("CHRONA_VERSION_CODE")?.toInt() ?: 1
val chronaVersionName = env("CHRONA_VERSION_NAME") ?: "1.0.0"

// Release signing material is supplied by CI only and never stored in Git.
val keystoreFile = env("ANDROID_KEYSTORE_FILE")
val keystorePassword = env("ANDROID_KEYSTORE_PASSWORD")
val releaseSigningAvailable = keystoreFile != null && keystorePassword != null

// "37.2" -> (37, 2), "37" -> (37, null); anything else is a preview codename.
val compileSdkRelease = Regex("""(\d+)(?:\.(\d+))?""").matchEntire(compileSdkSpec)

android {
  namespace = "com.android.deskclock"

  if (compileSdkRelease != null) {
    compileSdk = compileSdkRelease.groupValues[1].toInt()
    compileSdkRelease.groupValues[2]
        .takeIf { it.isNotEmpty() }
        ?.let { compileSdkMinor = it.toInt() }
  } else {
    compileSdkPreview = compileSdkSpec
  }
  buildToolsOverride?.let { buildToolsVersion = it }

  defaultConfig {
    applicationId = "com.android.deskclock"
    minSdk = 29
    targetSdk = 37
    versionCode = chronaVersionCode
    versionName = chronaVersionName
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    if (releaseSigningAvailable) {
      create("release") {
        storeFile = file(keystoreFile!!)
        storePassword = keystorePassword
        keyAlias = "chrona-release"
        keyPassword = keystorePassword
        storeType = "PKCS12"
      }
    }
  }

  splits {
    abi {
      isEnable = true
      reset()
      include("arm64-v8a", "armeabi-v7a", "x86", "x86_64")
      isUniversalApk = true
    }
  }

  buildTypes {
    // Debug: main branch, Android SDK beta channel.
    debug {
      applicationIdSuffix = ".debug"
      versionNameSuffix = "-debug"
    }
    // Release: stable branch and v* tags, Android SDK stable channel.
    release {
      isMinifyEnabled = false
      isShrinkResources = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      if (releaseSigningAvailable) {
        signingConfig = signingConfigs.getByName("release")
      }
    }
    // Dev: dev branch, Android SDK dev channel. Debuggable, installs side by side.
    create("dev") {
      initWith(getByName("debug"))
      applicationIdSuffix = ".dev"
      versionNameSuffix = "-dev"
      matchingFallbacks += "debug"
    }
    // Canary: canary branch, Android SDK canary channel. Release-like but
    // signed with the release key when available, otherwise the debug key.
    create("canary") {
      initWith(getByName("release"))
      applicationIdSuffix = ".canary"
      versionNameSuffix = "-canary"
      signingConfig =
          if (releaseSigningAvailable) signingConfigs.getByName("release")
          else signingConfigs.getByName("debug")
      matchingFallbacks += "release"
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures { buildConfig = true }

  lint {
    abortOnError = true
    warningsAsErrors = false
    lintConfig = file("lint.xml")
    checkReleaseBuilds = true
  }

  testOptions { unitTests.isIncludeAndroidResources = true }
}

kotlin {
  jvmToolchain(17)
  compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }
}

dependencies {
  implementation(libs.androidx.annotation)
  implementation(libs.androidx.collection)
  implementation(libs.androidx.arch.core.common)
  implementation(libs.androidx.lifecycle.common)
  implementation(libs.androidx.lifecycle.runtime)
  implementation(libs.androidx.core)
  implementation(libs.androidx.core.remoteviews)
  implementation(libs.androidx.coordinatorlayout)
  implementation(libs.androidx.interpolator)
  implementation(libs.androidx.loader)
  implementation(libs.androidx.vectordrawable)
  implementation(libs.androidx.percentlayout)
  implementation(libs.androidx.transition)
  implementation(libs.androidx.legacy.core.ui)
  implementation(libs.androidx.media)
  implementation(libs.androidx.legacy.v13)
  implementation(libs.androidx.preference)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.gridlayout)
  implementation(libs.androidx.recyclerview)
  implementation(libs.androidx.viewpager)
  implementation(libs.material)
  testImplementation(libs.junit)
  androidTestImplementation(libs.androidx.test.core)
  androidTestImplementation(libs.androidx.test.runner)
  androidTestImplementation(libs.androidx.test.rules)
  androidTestImplementation(libs.androidx.test.ext.junit)
  androidTestImplementation(libs.androidx.test.espresso.core)
}
