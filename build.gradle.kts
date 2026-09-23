plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.android") version "2.4.20" apply false
    id("com.diffplug.spotless") version "8.10.2"
}

spotless {
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt()
    }
    format("config") {
        target("**/*.yml", "**/*.yaml", "**/*.json", "**/*.properties")
        targetExclude(".gradle/**", "build/**", "app/build/**")
        trimTrailingWhitespace()
        endWithNewline()
    }
}
