# Fix Recurring Task Completion Behavior and Add History View

## Goal

Fix recurring tasks so they can be completed multiple times (currently stuck after first completion), and add a dedicated detail page for recurring tasks that shows completion history and allows editing.

## What I Already Know

From code inspection (`TaskRepository.kt:84-133`):

* **Current behavior**: Cycle-based recurring tasks with alarms advance to next due date on completion, but cycle-based tasks WITHOUT alarms mark `completed = true` and stay completed forever (line 108-115)
* **Count-based tasks**: Increment `completionCount` and mark completed
* There are two types of recurring tasks:
  - Cycle-based: Daily, Weekly, Monthly (line 13)
  - Count-based: Count (line 14)
* Task entity tracks: `repeatRule`, `completionCount`, `lastCompletedAtMillis`, `completed` flag
* No completion history table exists - only latest completion time is stored

## Problem Analysis

**Issue 1: Cycle-based tasks without alarms get stuck**
- When completed, they set `completed = true` (line 111)
- No mechanism to reset them for the next cycle
- User can't complete them again

**Issue 2: No completion history**
- Only `lastCompletedAtMillis` is stored
- Can't see all past completions
- Can't track streaks or patterns

## Requirements

### 1. 修复完成行为

**周期性事项 (每天/每周/每月):**
- 每个周期只能完成一次
- 本周期完成后，"完成"按钮禁用，直到下个周期
- `completed` 标记保持 `true`，直到周期边界
- 有闹钟和无闹钟的任务遵循相同逻辑
- 每次完成时递增 `completionCount` 和更新 `lastCompletedAtMillis`

**周期边界逻辑:**
- 每天: 午夜 (00:00 本地时间) 重置
- 每周: 周一 00:00 本地时间重置
- 每月: 每月 1 号 00:00 本地时间重置

**计数型事项:**
- 已经工作正常（可以无限次完成）

### 2. 添加完成历史追踪

创建新的 `TaskCompletion` 实体:
- 通过 `taskId` 关联到任务
- 记录 `completedAtMillis`
- 可选的 `note` 字段用于记录感想
- 完成时自动创建记录

### 3. 新的重复性事项详情页面

**访问方式**: 点击重复性事项卡片 (当 `repeatRule != None`)
- 非重复性事项: 点击 → 编辑表单（当前行为）
- 重复性事项: 点击 → 历史详情页面

**顶部区域:**
- 任务标题（可编辑）
- 象限、截止日期、闹钟设置（可编辑）
- 当前连击数 / 总完成次数

**历史区域:**
- 可滚动的完成记录列表
- 显示每次完成的日期/时间
- 如果有备注则显示
- 倒序排列（最新的在上面）

**操作:**
- 编辑按钮（打开编辑表单）
- 完成按钮（如果本周期尚未完成）
- 删除完成记录（需确认）

**完成流程:**
- 点击完成按钮 → 弹出备注对话框（保持现有行为）
- 用户填写"心得或复盘"（可留空）
- 保存 → 创建 `TaskCompletionEntity` 记录
- 本周期内禁用完成按钮

## Acceptance Criteria

* [ ] Cycle-based tasks without alarms can be completed multiple times
* [ ] Each completion increments `completionCount`
* [ ] Completion history is persisted in database
* [ ] Tapping recurring task opens detail page (not edit form)
* [ ] Detail page shows all past completions
* [ ] Can edit task from detail page
* [ ] Can complete task from detail page
* [ ] Non-recurring tasks still work as before

## Decisions

### 1. Completion Reset Timing ✓
**Decision**: Cycle boundary reset (Option B)
- Daily tasks: reset at midnight
- Weekly tasks: reset at start of week (Monday 00:00)
- Monthly tasks: reset at start of month (1st day 00:00)
- Prevents accidental duplicate completions within same cycle
- User can only complete once per cycle period

**Implementation**: Add a check before showing "Complete" button or allowing completion:
```kotlin
fun canCompleteInCurrentCycle(task: TaskEntity): Boolean {
    if (task.repeatRule == RepeatRule.None) return !task.completed
    if (task.repeatRule.isCountBased()) return true
    
    // For cycle-based: check if last completion was in current cycle
    val lastCompleted = task.lastCompletedAtMillis ?: return true
    return !isInSameCycle(lastCompleted, System.currentTimeMillis(), task.repeatRule)
}
```

### 2. History Page Access ✓
**Decision**: Click task card (Option A)
- Recurring tasks (`repeatRule != None`): Click → History detail page
- Non-recurring tasks: Click → Edit form (current behavior)
- History page has "Edit" button in top bar
- Clear navigation flow without extra steps

### 3. Completion Note Behavior ✓
**Decision**: Always prompt for note (Option A)
- Keep current behavior: completion dialog with note field
- Every completion requires going through note dialog
- User can leave note empty if desired
- Consistent with current UX expectations

## Requirements

### Database Changes

Add `TaskCompletion` entity:
```kotlin
@Entity(
    tableName = "task_completions",
    foreignKeys = [ForeignKey(
        entity = TaskEntity::class,
        parentColumns = ["id"],
        childColumns = ["taskId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val completedAtMillis: Long,
    val note: String?
)
```

Migration: version 7 → 8

### Repository Changes

Update `toggleCompleted()`:
- Create `TaskCompletionEntity` on each completion
- For cycle-based without alarms: keep `completed = false`
- Increment `completionCount` for all recurring types

Add `observeCompletions(taskId)` → Flow<List<TaskCompletionEntity>>

### UI Changes

- New `RecurringTaskDetailScreen` composable
- Navigation: if `task.repeatRule != None`, route to detail instead of edit
- History list with completion cards
- Edit button in top bar

## Out of Scope

* Streaks calculation/visualization (future enhancement)
* Completion editing (can only delete)
* Bulk completion operations
* Export completion history
* Completion reminders based on history patterns

## Technical Notes

Files to modify:
- `TaskModels.kt` - add `TaskCompletionEntity`
- `TaskDatabase.kt` - add DAO, migration 7→8
- `TaskRepository.kt` - update `toggleCompleted()`, add `observeCompletions()`
- `MainActivity.kt` - add detail screen, update navigation logic
