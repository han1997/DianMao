package `fun`.han1997.dianmao.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class RepeatRule(val label: String) {
    None("不重复"),
    Daily("每天"),
    Weekly("每周"),
    Monthly("每月"),
    Yearly("每年"),
    Count("计数");

    fun isCycleBased(): Boolean = this in listOf(Daily, Weekly, Monthly, Yearly)
    fun isCountBased(): Boolean = this == Count
}

enum class QuadrantType {
    ImportantUrgent,
    ImportantNotUrgent,
    NotImportantUrgent,
    NotImportantNotUrgent
}

enum class TaskPriority(val label: String, val shortLabel: String, val description: String, val colorHex: Long) {
    ImportantUrgent("重要且紧急", "重急", "今天必须推进", 0xFFA94332),
    ImportantNotUrgent("重要不紧急", "重要", "长期价值任务", 0xFF795C16),
    NotImportantUrgent("不重要但紧急", "紧急", "有时间压力", 0xFF2D6DA8),
    NotImportantNotUrgent("不重要不紧急", "低压", "可延后处理", 0xFF4E6D68);

    fun colorInt(): Int = colorHex.toInt()
}

data class TaskSubTask(
    val id: Long,
    val title: String,
    val completed: Boolean = false
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val detail: String? = null,
    val quadrant: QuadrantType,
    val dueLabel: String?,
    val dueAtMillis: Long?,
    val useCalendar: Boolean,
    val calendarEventId: Long? = null,
    val useAlarm: Boolean,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Int = 0,
    val reminderLeadExtraMinutes: Set<Int> = emptySet(),
    val repeatRule: RepeatRule = RepeatRule.None,
    val repeatInterval: Int = 1,
    val repeatWeekdays: Set<Int> = emptySet(),
    val subTasks: List<TaskSubTask> = emptyList(),
    val completionCount: Int = 0,
    val lastCompletedAtMillis: Long? = null,
    val completed: Boolean,
    val completionNote: String?,
    val archived: Boolean = false,
    val displayOrder: Int = 0,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)

data class TaskFormState(
    val title: String = "",
    val detail: String? = null,
    val quadrant: QuadrantType = QuadrantType.ImportantUrgent,
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null,
    val useCalendar: Boolean = false,
    val useAlarm: Boolean = false,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Set<Int> = emptySet(),
    val repeatRule: RepeatRule = RepeatRule.None,
    val repeatInterval: Int = 1,
    val repeatWeekdays: Set<Int> = emptySet(),
    val subTasks: List<TaskSubTask> = emptyList()
)

fun TaskFormState.normalizedTimeSettings(): TaskFormState {
    val hasDueDate = dueDateMillis != null
    val normalizedAlarm = useAlarm && hasDueDate
    val normalizedLocalReminder = useLocalReminder && hasDueDate
    val normalizedRepeat = repeatRule.takeIf { hasDueDate } ?: RepeatRule.None
    return copy(
        dueTimeMinutes = if (hasDueDate) dueTimeMinutes else null,
        useCalendar = useCalendar && hasDueDate,
        useAlarm = normalizedAlarm,
        useLocalReminder = normalizedLocalReminder,
        reminderLeadMinutes = if (normalizedLocalReminder) {
            reminderLeadMinutes.filter { it >= 0 }.toSet()
        } else {
            emptySet()
        },
        repeatRule = normalizedRepeat,
        repeatInterval = if (normalizedRepeat == RepeatRule.None) 1 else repeatInterval.coerceAtLeast(1),
        repeatWeekdays = if (normalizedRepeat == RepeatRule.Weekly) {
            repeatWeekdays.filter { it in 1..7 }.toSet()
        } else {
            emptySet()
        }
    )
}

/**
 * The scheduled lead minutes for a task: the primary stored value plus any
 * extra lead times, valid only when the local reminder is enabled. An empty
 * set means no reminder is scheduled.
 */
fun effectiveReminderLeadMinutes(
    useLocalReminder: Boolean,
    primaryLeadMinutes: Int,
    extraLeadMinutes: Set<Int>
): Set<Int> {
    if (!useLocalReminder) return emptySet()
    return (setOf(primaryLeadMinutes) + extraLeadMinutes.filter { it >= 0 }).toSet()
}

/**
 * Splits a selected lead set into its persisted form: the smallest lead time
 * stays in the primary INTEGER column, the rest go to the extras column. An
 * empty selection persists as the on-time default (primary 0, no extras).
 */
fun reminderLeadSetToStorage(leadMinutes: Set<Int>): Pair<Int, Set<Int>> {
    if (leadMinutes.isEmpty()) return Pair(0, emptySet())
    val primary = leadMinutes.filter { it >= 0 }.minOrNull() ?: 0
    return Pair(primary, (leadMinutes - primary).filter { it >= 0 }.toSet())
}

data class TaskUiModel(
    val id: Long,
    val title: String,
    val detail: String? = null,
    val quadrant: TaskPriority,
    val dueLabel: String?,
    val dueAtMillis: Long?,
    val useCalendar: Boolean,
    val calendarEventId: Long?,
    val useAlarm: Boolean,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Set<Int> = emptySet(),
    val repeatRule: RepeatRule,
    val repeatInterval: Int = 1,
    val repeatWeekdays: Set<Int> = emptySet(),
    val subTasks: List<TaskSubTask>,
    val completionCount: Int,
    val lastCompletedAtMillis: Long?,
    val completed: Boolean,
    val completionNote: String?,
    val archived: Boolean = false
)

fun TaskEntity.toUiModel(): TaskUiModel {
    return TaskUiModel(
        id = id,
        title = title,
        detail = detail,
        quadrant = TaskPriority.valueOf(quadrant.name),
        dueLabel = dueLabel,
        dueAtMillis = dueAtMillis,
        useCalendar = useCalendar,
        calendarEventId = calendarEventId,
        useAlarm = useAlarm,
        useLocalReminder = useLocalReminder,
        reminderLeadMinutes = effectiveReminderLeadMinutes(useLocalReminder, reminderLeadMinutes, reminderLeadExtraMinutes),
        repeatRule = repeatRule,
        repeatInterval = repeatInterval,
        repeatWeekdays = repeatWeekdays,
        subTasks = subTasks,
        completionCount = completionCount,
        lastCompletedAtMillis = lastCompletedAtMillis,
        completed = completed,
        completionNote = completionNote,
        archived = archived
    )
}

@Entity(
    tableName = "task_completions",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"])]
)
data class TaskCompletionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val completedAtMillis: Long,
    val note: String?
)

/** Review-view projection: one completion joined with its task's title/quadrant. */
data class CompletionWithTask(
    val taskId: Long,
    val completedAtMillis: Long,
    val note: String?,
    val taskTitle: String,
    val taskQuadrant: QuadrantType
)
