# Directory Structure

This project is a single-module Android application. Treat `app/` as the only
runtime package unless the Gradle module layout changes.

## Module Layout

Reference files:

- `settings.gradle.kts` includes only the `:app` module.
- `app/build.gradle.kts` applies Android application, Kotlin Android, KSP,
  Compose, and Room dependencies.
- `app/src/main/AndroidManifest.xml` owns Android permissions, receivers, and
  application metadata.

Do not introduce new Gradle modules for small features. Add modules only when a
real ownership boundary appears and the build files, package specs, and tests
are updated together.

## Source Packages

Runtime Kotlin code lives under `app/src/main/java/fun/han1997/dianmao/`.

| Package | Ownership |
|---|---|
| root package | `MainActivity` entry + `DianMaoApp` assembly (repository/scheduler wiring, overlays, back handling) |
| `ui.theme` | Compose theme, light color scheme, shapes, quiet-border and quiet text-field helpers |
| `ui.components` | Shared composables (`IconTile`, `HeaderActionButton`, `TaskRow`, subtask editors, drag state) |
| `ui.logic` | Pure UI models/resolvers (`InAppBackTarget`, `HomeSmartView`, `buildTimeFocusItems`, platform helpers) |
| `ui.screens` | Full-screen composables (`HomeScreen`, `QuadrantDetailScreen`, `TaskDetailScreen`) |
| `ui.panels` | Overlay panels and dialogs (`AddTaskPanel`, `BackupPanel`, `ArchivedPanel`, dialogs) |
| `data` | Room entities, DAOs, database migrations, repositories, backup codec, date/repeat calculators |
| `alerts` | AlarmManager scheduling, notification channel setup, reminder delivery, boot restoration |
| `calendar` | Calendar Provider read/write/update/delete and calendar insert/edit intents |
| `backup` | Backup settings persistence and WebDAV transport |

Keep Android platform side effects in the package that owns the platform API:
Calendar Provider code belongs in `calendar`, AlarmManager/notification code in
`alerts`, and WebDAV HTTP code in `backup`. The repository may coordinate state
changes, but it should not directly construct platform intents or notifications.

## Data Flow

The usual write flow is:

1. Compose form state in `MainActivity.kt`.
2. `TaskFormState` enters `TaskRepository`.
3. Repository normalizes and persists `TaskEntity` through `TaskDao`.
4. App flow schedules best-effort side effects through `ReminderScheduler` or
   `CalendarWriter`.

Room is the durable source of truth. External systems such as AlarmManager,
Calendar Provider, notification manager, and WebDAV are rebuilt from or synced
around Room state.

## Resource Layout

Use the standard Android source set:

- `app/src/main/res/values/` for themes, colors, and strings.
- `app/src/main/res/drawable/` and `mipmap-*` for launcher icon assets.
- `app/src/main/res/xml/` for Android backup rules.
- `app/schemas/fun.han1997.dianmao.data.TaskDatabase/` for exported Room
  schemas. Commit a new schema JSON whenever the database version changes.

## Tests

Local JVM tests live in `app/src/test/java/fun/han1997/dianmao/` and currently
cover pure or framework-light logic:

- `RepeatReminderCalculatorTest.kt`
- `InAppBackTargetTest.kt`
- `CalendarWriterSelectionTest.kt`

Use JVM tests for pure Kotlin helpers, state resolvers, ranking functions, and
date calculations. Avoid local JVM tests for code that constructs Android SDK
framework objects not backed by test implementations.

Instrumentation tests live in `app/src/androidTest/`. Add instrumentation or
Robolectric-style coverage when behavior depends on Android framework runtime
objects.

## Naming Conventions

- Kotlin files use PascalCase matching the main type or feature helper, for
  example `TaskRepository.kt`, `CalendarWriter.kt`, and
  `RepeatReminderCalculator.kt`.
- Room database types use the `Task*` prefix because task persistence is the
  app's central domain.
- Broadcast receivers use `*Receiver`, schedulers use `*Scheduler`, and pure
  calculation helpers use `*Calculator`.
- Keep package names lowercase and aligned with the existing ownership
  boundary.

## Common Mistakes

- Placing platform code in `TaskDao`; DAOs should only read and write Room.
- Adding a persisted field without updating `TaskEntity`, `TaskUiModel`,
  `toUiModel()`, `TaskBackupCodec`, the migration chain, and exported schema.
- Adding a new source package when an existing owner already matches the
  behavior.
- Treating `MainActivity.kt` as the right home for reusable pure logic. Extract
  pure date, ranking, or serialization logic into a package helper when it needs
  direct tests.
