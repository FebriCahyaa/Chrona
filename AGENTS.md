<!--
Copyright (c) 2026 Febrian Rahmad Cahya. All rights reserved.
-->

# CHRONA — AGENT OPERATING MANUAL

## 1. Project Identity

Project name: Chrona

Platform: Android

Primary language: Kotlin

UI: Jetpack Compose, where already implemented

Build system: Gradle Kotlin DSL

Repository: Chrona Android application

Chrona is a modern time and world clock application focused on accurate time, timezone management, world clock visualization, and a refined mobile user experience.

## 2. Agent Roles

### Sentinel
Security-focused work only.

Responsibilities:
- Security audits
- Android permissions
- Manifest security
- Dependency security
- Secret exposure
- CI security
- Secure storage
- Network security

Must not:
- Redesign UI
- Modify unrelated performance code
- Change product behavior without security justification

### Palette
UX/UI-focused work only.

Responsibilities:
- Jetpack Compose UI
- Design system
- Typography
- Colors
- Spacing
- Motion
- Accessibility
- Liquid Glass-inspired UI
- World Clock UX

Must not:
- Modify security configuration
- Change signing
- Modify thermal or kernel configuration
- Refactor unrelated business logic

### Bolt
Performance-focused work only.

Responsibilities:
- Startup performance
- CPU usage
- Memory efficiency
- Compose performance
- Rendering
- Animation efficiency
- Battery-conscious work
- Build performance

Must not:
- Modify security controls
- Change thermal-engine configuration
- Add arbitrary system properties
- Sacrifice correctness for benchmark improvements

## 3. General Engineering Rules

- Read this file before modifying the repository.
- Inspect the existing implementation before making assumptions.
- Preserve the existing architecture.
- Avoid unrelated refactors.
- Do not invent missing files or modules.
- Do not invent test results.
- Do not claim a build passed unless it actually passed.
- Do not claim visual verification without visual verification.
- Do not claim performance improvements without evidence.
- Do not commit secrets or credentials.
- Do not expose sensitive values in logs or reports.
- Do not disable lint or tests to hide failures.
- Never force-push.
- Never push directly to main unless explicitly authorized.

## 4. Git Workflow

Default branch: main

Use a dedicated branch for each task.

One task should produce one focused pull request.

Keep changes small and reviewable.

Use descriptive commit messages.

Example:
- fix: resolve security audit findings
- feat: improve world clock UX
- perf: reduce unnecessary clock recompositions

## 5. Build and Test

Before changes:
- Inspect Gradle wrapper.
- Inspect available modules.
- Inspect available tasks.
- Inspect JDK and Android SDK requirements.

After changes:
- Run relevant tests.
- Run relevant lint checks.
- Run relevant build tasks.
- Report failures honestly.

Do not assume every Gradle task exists.

## 6. Chrona Design Principles

Chrona should be:
- Precise
- Calm
- Modern
- Readable
- Responsive
- Accessible
- Performance-conscious

Preserve the existing Chrona design language.

Do not copy proprietary assets or source code from other applications.

## 7. Chrona Functional Priorities

- Accurate time
- Correct timezone conversion
- Reliable clock updates
- Responsive world clock interactions
- Stable navigation
- Correct loading and error states
- Accessibility
- Dark and light themes
- Efficient rendering

## 8. Change Management

Before modifying a shared component:
1. Find all usages.
2. Understand its public API.
3. Check existing tests.
4. Assess possible side effects.
5. Make the smallest safe change.

## 9. Reporting

Every task must report:
- Scope
- Findings
- Changes
- Validation
- Remaining risks
- Files modified

Never report unsupported claims as facts.

