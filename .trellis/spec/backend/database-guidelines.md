# Database Guidelines

> Database patterns and conventions for this project.

---

## Overview

DianMao stores task data with Room. `TaskDatabase` owns schema versioning,
`TaskEntity` is the persisted contract, and `TaskUiModel` is the UI-facing
projection. Any persisted field added to `TaskEntity` must be carried through
the Room migration chain, `toUiModel()`, the backup codec, and the exported
Room schema.

---

## Query Patterns

Use DAO methods for database reads/writes. Keep Android platform side effects
outside DAO methods; repositories may coordinate local entity updates, but
Calendar Provider, AlarmManager, and notification calls stay in their platform
modules or app flow.

---

## Migrations

## Scenario: Room Task Schema Field Addition

### 1. Scope / Trigger

Trigger this contract whenever a field is added, removed, renamed, or has its
nullability/default changed in `TaskEntity`.

### 2. Signatures

* Database annotation: `@Database(entities = [TaskEntity::class], version = <next>, exportSchema = true)`
* Migration object: `Migration(<from>, <to>) { override fun migrate(db: SupportSQLiteDatabase) }`
* Registration: `.addMigrations(MIGRATION_1_2, MIGRATION_2_3, ...)`
* Entity projection: `fun TaskEntity.toUiModel(): TaskUiModel`
* Schema output: `app/schemas/fun.han1997.dianmao.data.TaskDatabase/<version>.json`

### 3. Contracts

* New nullable fields use nullable Kotlin types and nullable SQLite columns.
* New non-null fields must define a SQLite `DEFAULT` in the migration.
* The migration chain must support every adjacent version from the previous
  release to the new release.
* The UI model must include persisted fields that later app flows need to
  update external systems.
* Room schema export stays enabled. Commit the generated schema JSON whenever
  the database schema changes.
* Backup import/export must be updated for every persisted task field.

### 4. Validation & Error Matrix

* Missing adjacent migration -> Room opens fail at runtime.
* Entity field missing from `toUiModel()` -> UI/app flow silently loses stored data.
* Non-null column without default -> migration fails on existing rows.
* Schema JSON not committed -> future migration diffs lose the baseline.
* Backup codec missing a new field -> backup/restore silently drops user data.
* Platform side effect stored only in memory -> later edits cannot target the
  same external record.

### 5. Good/Base/Bad Cases

* Good: Add `calendarEventId: Long?`, migrate with `ALTER TABLE tasks ADD COLUMN calendarEventId INTEGER`, and project it into `TaskUiModel`.
* Base: Add a UI-only field to `TaskFormState`; no Room migration is needed.
* Bad: Insert Calendar Provider events without storing the returned event id,
  then attempt future updates by matching title/time.

### 6. Tests Required

* Run `:app:compileDebugKotlin` or `:app:assembleDebug` so Room/KSP validates
  the entity shape and updates exported schema JSON.
* Run `:app:lintDebug` when migrations touch platform side effects or
  permission-gated APIs.
* If Room migration tests are added later, assert old rows survive and new
  columns contain the intended default/null value.

### 7. Wrong vs Correct

#### Wrong

```kotlin
@Database(entities = [TaskEntity::class], version = 3)
abstract class TaskDatabase : RoomDatabase()
```

This bumps the schema version but does not register a `Migration(2, 3)`.

#### Correct

```kotlin
@Database(entities = [TaskEntity::class], version = 3, exportSchema = true)
abstract class TaskDatabase : RoomDatabase() {
    companion object {
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tasks ADD COLUMN calendarEventId INTEGER")
            }
        }
    }
}
```

---

## Naming Conventions

Use Kotlin property names as Room column names unless there is a strong reason
to annotate a column explicitly. Keep migration SQL aligned with the property
name, for example `calendarEventId`.

---

## Common Mistakes

* Updating `TaskEntity` without updating `TaskUiModel` and `toUiModel()`.
* Updating `TaskEntity` without updating `TaskBackupCodec`.
* Bumping the Room database version without adding the adjacent migration.
* Forgetting to commit `app/schemas/**/<version>.json` after schema changes.
* Managing external records without persisting their stable external id.

## Scenario: Task Backup JSON

### 1. Scope / Trigger

Trigger this contract when adding backup/export/import behavior or changing any
field in `TaskEntity`.

### 2. Signatures

* Export: `TaskRepository.exportBackupJson(): String`
* Import: `TaskRepository.importBackupJson(json: String): Int`
* Codec: `TaskBackupCodec.encode(tasks): String`
* Codec: `TaskBackupCodec.decodeForImport(json): List<TaskEntity>`

### 3. Contracts

* Backup payloads are UTF-8 JSON with `format`, `backupVersion`,
  `databaseVersion`, `createdAtMillis`, and `tasks`.
* Import replaces all local task rows inside a Room transaction.
* Import clears `calendarEventId`; Calendar Provider ids are device-local and
  must not be restored across devices.
* Import preserves task ids so app-owned alarms can be rescheduled consistently
  after restore.

### 4. Validation & Error Matrix

* Unsupported `format` -> reject import.
* Future `backupVersion` -> reject import.
* Invalid enum value -> reject import.
* Local file/WebDAV read failure -> surface an error, keep existing Room data.

### 5. Good/Base/Bad Cases

* Good: Decode the whole payload first, then replace rows in a transaction.
* Base: Empty backup imports as an empty task list.
* Bad: Delete local rows before validating the backup JSON.

### 6. Tests Required

* Run `:app:compileDebugKotlin` because the codec uses Android JSON APIs.
* Run `:app:assembleDebug` to validate the backup UI, repository, and schema
  generation together.

### 7. Wrong vs Correct

#### Wrong

```kotlin
dao.deleteAll()
val tasks = TaskBackupCodec.decodeForImport(json)
dao.insertAll(tasks)
```

Invalid JSON would erase local data.

#### Correct

```kotlin
val tasks = TaskBackupCodec.decodeForImport(json)
db.withTransaction {
    dao.deleteAll()
    dao.insertAll(tasks)
}
```
