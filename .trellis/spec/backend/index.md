# Backend Development Guidelines

This project is a single-module Android app. The backend spec layer covers the
local persistence, platform integration, Compose workflow, backup, and quality
rules that future agents must follow when changing DianMao.

## Guidelines Index

| Guide | Description | Status |
|---|---|---|
| [Directory Structure](./directory-structure.md) | Android module layout, package ownership, source/test placement | Active |
| [Database Guidelines](./database-guidelines.md) | Room schema, migrations, backup codec, recurring completion contracts | Active |
| [Error Handling](./error-handling.md) | Permission-gated platform calls, reminders, WebDAV failure behavior | Active |
| [Compose Patterns](./compose-patterns.md) | BackHandler state capture, overlay navigation, edge-to-edge safe areas, compact accessible layout | Active |
| [Quality Guidelines](./quality-guidelines.md) | Required checks, testing strategy, data/platform/Compose review rules | Active |
| [Logging Guidelines](./logging-guidelines.md) | Privacy-safe Android logging conventions | Active |
| [Release Packaging](./release-packaging.md) | Version source, bumpVersion task, APK naming, upload-key signing | Active |

## Quality Check

Before finishing code changes, read the guide that matches the touched area and
run the relevant project checks:

- `.\gradlew.bat :app:testDebugUnitTest --no-daemon`
- `.\gradlew.bat :app:lintDebug --no-daemon`
- `.\gradlew.bat :app:assembleDebug --no-daemon`
- `git diff --check`

Use sequential Gradle invocations when tasks touch KSP output.

## Spec Maintenance

Update these specs when a task introduces a durable project rule, a repeated
bug pattern, or a new ownership boundary. Keep guidance source-backed with real
file paths from this repository.
