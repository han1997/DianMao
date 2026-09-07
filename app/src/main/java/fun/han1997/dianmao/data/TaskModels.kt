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
    Count("计数");

    fun isCycleBased(): Boolean = this in listOf(Daily, Weekly, Monthly)
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
    val quadrant: QuadrantType,
    val dueLabel: String?,
    val dueAtMillis: Long?,
    val useCalendar: Boolean,
    val calendarEventId: Long? = null,
    val useAlarm: Boolean,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Int = 0,
    val repeatRule: RepeatRule = RepeatRule.None,
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
    val quadrant: QuadrantType = QuadrantType.ImportantUrgent,
    val dueDateMillis: Long? = null,
    val dueTimeMinutes: Int? = null,
    val useCalendar: Boolean = false,
    val useAlarm: Boolean = false,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Int = 0,
    val repeatRule: RepeatRule = RepeatRule.None,
    val subTasks: List<TaskSubTask> = emptyList()
)

fun TaskFormState.normalizedTimeSettings(): TaskFormState {
    val hasDueDate = dueDateMillis != null
    val normalizedAlarm = useAlarm && hasDueDate
    val normalizedLocalReminder = useLocalReminder && hasDueDate
    return copy(
        dueTimeMinutes = if (hasDueDate) dueTimeMinutes else null,
        useCalendar = useCalendar && hasDueDate,
        useAlarm = normalizedAlarm,
        useLocalReminder = normalizedLocalReminder,
        reminderLeadMinutes = if (normalizedLocalReminder) reminderLeadMinutes else 0,
        repeatRule = if (normalizedAlarm) repeatRule else RepeatRule.None
    )
}

data class TaskUiModel(
    val id: Long,
    val title: String,
    val quadrant: TaskPriority,
    val dueLabel: String?,
    val dueAtMillis: Long?,
    val useCalendar: Boolean,
    val calendarEventId: Long?,
    val useAlarm: Boolean,
    val useLocalReminder: Boolean = false,
    val reminderLeadMinutes: Int = 0,
    val repeatRule: RepeatRule,
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
        quadrant = TaskPriority.valueOf(quadrant.name),
        dueLabel = dueLabel,
        dueAtMillis = dueAtMillis,
        useCalendar = useCalendar,
        calendarEventId = calendarEventId,
        useAlarm = useAlarm,
        useLocalReminder = useLocalReminder,
        reminderLeadMinutes = reminderLeadMinutes,
        repeatRule = repeatRule,
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
