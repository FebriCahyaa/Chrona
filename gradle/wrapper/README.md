# Gradle Bootstrap

The repository uses Gradle 9.7.1. `gradlew` is a network/bootstrap launcher that runs an installed Gradle binary when available and otherwise downloads the pinned Gradle 9.7.1 distribution after verifying its configured SHA-256 through the distribution URL contract.

This environment could not materialize the official Gradle wrapper JAR because outbound network access is unavailable. A standard Gradle wrapper can be regenerated locally with `gradle wrapper --gradle-version 9.7.1 --distribution-type bin` without changing the project version policy.
