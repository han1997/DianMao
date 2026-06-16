# Support recurring tasks without time

## Goal

Allow users to set repeat rules (Daily/Weekly/Monthly) on tasks without requiring a specific due date/time. This enables "循环事项" (recurring responsibilities) distinct from "重复提醒" (recurring alarms).

## What I already know

From codebase inspection:

* `RepeatRule` enum exists: None, Daily, Weekly, Monthly (TaskModels.kt)
* Current UI flow:
  - Clearing time (onClear in DateTimePickerRow) resets repeatRule to None
  - RepeatRulePicker is only enabled when `dueDateMillis != null && useAlarm` (line 1590 in MainActivity.kt)
* Data model already supports `repeatRule` independent of `useAlarm` (TaskEntity has separate fields)
* There are two semantic uses:
  1. **Alarm-based recurrence**: useAlarm=true + repeatRule → AlarmManager scheduling
  2. **Conceptual recurrence**: repeatRule without time → just indicates "this repeats" (not yet implemented)

From PRODUCT.md context:

* Product emphasizes "capture before classification"
* "Same-day tasks and long-term responsibilities should share a consistent item model"
* Success means clarity about what matters now, structured enough to preserve context

## Assumptions (temporary)

* Recurring tasks without time are purely visual/organizational (no AlarmManager involvement)
* Users will manually mark them complete each cycle
* Display format for "every week" task without specific time: show repeat badge, no specific due time

## Decision: Two repeat semantics (ADR-lite)

**Context**: Users need both "计数型" (count-based) and "周期型" (cycle-based) recurring tasks.

**Decision**: Extend RepeatRule enum to support both semantics explicitly.

**Consequences**: 
- More RepeatRule options but clearer user intent
- Need UI to present both types
- Completion logic branches based on rule type

## Requirements

### Data Model
* Extend RepeatRule enum:
  - **Cycle-based** (周期型): `Daily`, `Weekly`, `Monthly` - auto-reset to incomplete after period expires
  - **Count-based** (计数型): `DailyCount`, `WeeklyCount`, `MonthlyCount` - manual completion, track count
* Add field to TaskEntity: `completionCount: Int = 0` (for count-based tracking)
* Add field to TaskEntity: `lastCompletedAtMillis: Long? = null` (to detect period expiration)

### UI Changes
* RepeatRulePicker:
  - Remove dependency on `dueDateMillis != null && useAlarm`
  - Group options: "周期循环" vs "计数统计"
  - Enabled independently of time setting
* DateTimePickerRow onClear:
  - Do NOT reset repeatRule to None
  - Only clear useCalendar and useAlarm
* Task display:
  - Show repeat badge for tasks with repeatRule != None (even without time)
  - For count-based: show completion count (e.g., "已完成 3 次")
  - For cycle-based: show cycle label (e.g., "每周")

### Completion Logic
* **Cycle-based (Daily/Weekly/Monthly)**:
  - On completion: mark completed, record `lastCompletedAtMillis`, show in completion list
  - Background check (app launch): if current period > completed period, auto-reset to incomplete
  - Completion history preserved in archived records
* **Count-based (DailyCount/WeeklyCount/MonthlyCount)**:
  - On completion: increment `completionCount`, mark completed, show in completion list
  - No auto-reset behavior
  - Display count in task detail view

### Alarm Integration
* Alarm-based repeat (useAlarm=true) works only with time set
* Alarm repeat is orthogonal to cycle/count semantics

## Open Questions

*All resolved*

## UI Design

### RepeatRulePicker Labels
```
不重复
每天    每周    每月          (cycle-based, auto-reset)
记数·每天  记数·每周  记数·每月   (count-based, manual)
```

* Simple, minimal labels
* "记数·" prefix clearly distinguishes count-based type
* Single row layout, grouped visually by behavior

## Acceptance Criteria

* [ ] RepeatRule enum extended with 6 values: Daily, Weekly, Monthly, DailyCount, WeeklyCount, MonthlyCount
* [ ] TaskEntity schema updated with completionCount and lastCompletedAtMillis fields
* [ ] Database migration created for schema changes
* [ ] RepeatRulePicker shows grouped options (cycle vs count) and works without time
* [ ] Clearing time preserves repeatRule setting
* [ ] Cycle-based tasks auto-reset to incomplete after period expires
* [ ] Count-based tasks increment counter on completion
* [ ] Task display shows appropriate badge/count for recurring tasks
* [ ] Completion history visible in detail view for recurring tasks
* [ ] Existing alarm-based repeat behavior unchanged

## Definition of Done (team quality bar)

* UI changes implemented in AddTaskPanel
* Visual display updated for recurring tasks without time
* Manual testing: create recurring task without time, verify it displays correctly
* No regression in alarm-based recurrence

## Out of Scope (explicit)

* Auto-recreation of recurring tasks on completion
* Cycle tracking / completion history
* Calendar sync for time-less recurring tasks
* Smart scheduling suggestions

## Technical Notes

* Files to modify:
  - `data/TaskModels.kt`: Extend RepeatRule enum, add completionCount/lastCompletedAtMillis to TaskEntity
  - `data/AppDatabase.kt`: Add migration for schema changes
  - `MainActivity.kt`:
    - AddTaskPanel: RepeatRulePicker enable logic (remove time dependency), onClear behavior (preserve repeatRule)
    - CompactQuadrantTask, TaskRow: Display repeat badge and count
    - DianMaoApp: Add background cycle-check logic on app launch
  - `data/TaskRepository.kt`: Add methods for cycle-reset logic, completion count increment
* Current constraints to remove:
  - Line ~1563: `onClear` sets `repeatRule = RepeatRule.None`
  - Line ~1590: RepeatRulePicker `enabled = dueDateMillis != null && useAlarm`
* New behavior:
  - App launch: check all cycle-based tasks, if lastCompletedAtMillis is in previous period, reset to incomplete
  - Task completion: branch on repeatRule type (increment count vs record timestamp)
  
## Implementation Plan

**PR1: Data model + migration**
- Extend RepeatRule enum (6 values)
- Add completionCount, lastCompletedAtMillis fields to TaskEntity
- Create and test database migration
- Update TaskFormState, TaskUiModel to support new fields

**PR2: UI decoupling + RepeatRulePicker redesign**
- Remove time dependency from RepeatRulePicker enablement
- Update RepeatRulePicker to show 7 options (None + 6 repeat types)
- Fix onClear to preserve repeatRule
- Update display components to show repeat badge without time

**PR3: Completion logic + cycle reset**
- Implement count increment logic in TaskRepository
- Implement cycle-reset check on app launch
- Add completion history display in detail view
- Test both cycle and count behaviors
