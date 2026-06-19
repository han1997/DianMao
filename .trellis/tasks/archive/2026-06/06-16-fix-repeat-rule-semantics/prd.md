# Fix repeat rule semantics

## Goal

Correct the repeat rule design based on clarified requirements:
1. Count-based tasks don't need Daily/Weekly/Monthly variants - just one generic "计数" type
2. Alarm-based repeat tasks should continue triggering alarms in next cycles

## Current Issues

From previous implementation (commits d67706b, c864885, 83a4dd2):
- RepeatRule has DailyCount/WeeklyCount/MonthlyCount (too complex, unnecessary)
- Alarm repeat logic doesn't handle cycle-based tasks (alarms stop after first completion)

## Requirements

### RepeatRule Enum Simplified
- Remove: `DailyCount`, `WeeklyCount`, `MonthlyCount`
- Add: `Count` (generic count-based, no cycle)
- Keep: `None`, `Daily`, `Weekly`, `Monthly` (cycle-based)

Total: 5 values (None + 3 cycle + 1 count)

### Completion Semantics

**Cycle-based (Daily/Weekly/Monthly):**
- Without alarm: mark complete, auto-reset next period
- With alarm: mark complete, auto-reset next period, **reschedule alarm for next cycle**

**Count-based (Count):**
- Increment completionCount on each completion
- No auto-reset behavior
- No alarm scheduling (count tasks are typically not time-based)

**None:**
- Normal completion toggle

### UI Changes
- RepeatRulePicker labels: `不重复 | 每天 每周 每月 | 计数`
- Remove "记数·每天" etc labels

### Alarm Integration Fix
- When cycle-based task with alarm completes:
  - Calculate next due time based on repeatRule
  - Update task dueAtMillis to next cycle
  - Reschedule alarm via ReminderScheduler
- Count-based tasks don't support alarms (or disable alarm when Count selected)

## Acceptance Criteria

* [ ] RepeatRule enum has 5 values (None, Daily, Weekly, Monthly, Count)
* [ ] Database migration removes old count variants (data preserved as Count)
* [ ] RepeatRulePicker shows simplified 5 options
* [ ] Cycle tasks with alarms reschedule for next period on completion
* [ ] Count tasks increment counter, no auto-reset
* [ ] Existing cycle-reset logic works for Daily/Weekly/Monthly

## Technical Notes

- Revert parts of commit d67706b (DailyCount/WeeklyCount/MonthlyCount)
- Update TaskRepository toggleCompleted/saveCompletionNote logic
- Add alarm rescheduling in completion handlers
- Migration: map DailyCount/WeeklyCount/MonthlyCount → Count

## Out of Scope

- Count tasks with alarms (keep simple: count = no time, cycle = can have time+alarm)
