plugins {
  id("com.android.application") version "9.4.1" apply false
  // AGP 9 compiles Kotlin itself (android.builtInKotlin). Declaring the Kotlin
  // plugin here only pins the Kotlin Gradle Plugin version on the classpath.
  // Stay below 2.4.20: the CodeQL bundle rejects it during extraction.
  id("org.jetbrains.kotlin.android") version "2.4.10" apply false
  id("com.diffplug.spotless") version "8.10.2"
}

spotless {
  kotlinGradle {
    target("*.gradle.kts", "app/*.gradle.kts")
    ktfmt("0.64")
  }
  format("config") {
    target("**/*.yml", "**/*.yaml", "**/*.json", "**/*.properties")
    targetExclude(".gradle/**", "build/**", "app/build/**", "**/node_modules/**")
    trimTrailingWhitespace()
    endWithNewline()
  }
}
