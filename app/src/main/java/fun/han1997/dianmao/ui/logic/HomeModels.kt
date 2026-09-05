package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.QuadrantType
import `fun`.han1997.dianmao.data.RepeatReminderCalculator
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.ArchivedPanel
import `fun`.han1997.dianmao.BackupPanel
import androidx.compose.foundation.layout.size
import androidx.compose.ui.graphics.Color
import java.time.Instant
import java.time.ZoneId

internal enum class InAppBackTarget {
    TaskDetail,
    BackupPanel,
    ArchivedPanel,
    TaskPanel,
    QuadrantDetail
}

internal enum class TimeFocusKind {
    Overdue,
    Today,
    Upcoming
}

internal data class TimeFocusItem(
    val task: TaskUiModel,
    val kind: TimeFocusKind,
    val dueAtMillis: Long,
    val dueLabel: String?
)

internal enum class HomeSmartView(val label: String) {
    Quadrants("四象限"),
    Today("今天"),
    Upcoming("近期"),
    Inbox("收集箱")
}

internal data class HomeSmartTaskItem(
    val task: TaskUiModel,
    val dueAtMillis: Long?,
    val dueLabel: String?
)

internal fun buildTimeFocusItems(
    tasks: List<TaskUiModel>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    limit: Int = 3
): List<TimeFocusItem> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    return tasks.mapNotNull { task ->
        if (!task.isOpenTask()) return@mapNotNull null
        val dueAt = effectiveDueAtMillis(task, nowMillis, zoneId) ?: return@mapNotNull null
        val kind = when {
            dueAt < nowMillis -> TimeFocusKind.Overdue
            dueAt < tomorrowStartMillis -> TimeFocusKind.Today
            else -> TimeFocusKind.Upcoming
        }
        TimeFocusItem(
            task = task,
            kind = kind,
            dueAtMillis = dueAt,
            dueLabel = task.displayDueLabel(dueAt, zoneId)
        )
    }.sortedWith(
        compareBy<TimeFocusItem>(
            { it.kind.rank },
            { it.dueAtMillis }
        )
    ).take(limit)
}

internal fun buildHomeSmartTaskItems(
    tasks: List<TaskUiModel>,
    view: HomeSmartView,
    query: String,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<HomeSmartTaskItem> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val upcomingEndMillis = today.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val normalizedQuery = query.trim()

    val indexedItems = tasks.mapIndexedNotNull { index, task ->
        if (!task.isOpenTask() || !task.matchesHomeSmartQuery(normalizedQuery)) {
            return@mapIndexedNotNull null
        }
        val dueAt = effectiveDueAtMillis(task, nowMillis, zoneId)
        val included = when (view) {
            HomeSmartView.Quadrants -> true
            HomeSmartView.Today -> dueAt != null && dueAt < tomorrowStartMillis
            HomeSmartView.Upcoming -> dueAt != null &&
                dueAt >= tomorrowStartMillis &&
                dueAt < upcomingEndMillis
            HomeSmartView.Inbox -> task.dueAtMillis == null
        }
        if (!included) {
            null
        } else {
            index to HomeSmartTaskItem(
                task = task,
                dueAtMillis = dueAt,
                dueLabel = dueAt?.let { task.displayDueLabel(it, zoneId) }
            )
        }
    }

    return when (view) {
        HomeSmartView.Today,
        HomeSmartView.Upcoming -> indexedItems.sortedWith(
            compareBy<Pair<Int, HomeSmartTaskItem>>(
                { it.second.dueAtMillis ?: Long.MAX_VALUE },
                { it.second.task.id }
            )
        )
        HomeSmartView.Quadrants,
        HomeSmartView.Inbox -> indexedItems.sortedBy { it.first }
    }.map { it.second }
}

internal fun TaskUiModel.isOpenTask(): Boolean = !completed && !archived

internal fun effectiveDueAtMillis(
    task: TaskUiModel,
    nowMillis: Long,
    zoneId: ZoneId
): Long? {
    val storedDueAt = task.dueAtMillis ?: return null
    return RepeatReminderCalculator.currentCycleDueAt(
        currentMillis = storedDueAt,
        repeatRule = task.repeatRule,
        nowMillis = nowMillis,
        zoneId = zoneId
    )
}

internal fun TaskUiModel.displayDueLabel(dueAtMillis: Long, zoneId: ZoneId): String {
    val calculatedDueLabel = DateTimeFormatters.labelFor(
        dateMillis = midnightMillis(dueAtMillis, zoneId),
        minutesOfDay = minutesOfDay(dueAtMillis, zoneId)
    )
    return if (repeatRule.isCycleBased()) {
        calculatedDueLabel
    } else {
        dueLabel ?: calculatedDueLabel
    }
}

internal fun TaskUiModel.matchesHomeSmartQuery(query: String): Boolean {
    if (query.isBlank()) return true
    return title.contains(query, ignoreCase = true) ||
        subTasks.any { it.title.contains(query, ignoreCase = true) }
}

internal val TimeFocusKind.rank: Int
    get() = when (this) {
        TimeFocusKind.Overdue -> 0
        TimeFocusKind.Today -> 1
        TimeFocusKind.Upcoming -> 2
    }

internal fun resolveInAppBackTarget(
    showBackupPanel: Boolean,
    showArchivedPanel: Boolean,
    isTaskPanelVisible: Boolean,
    hasActivePriority: Boolean,
    taskForDetail: Boolean
): InAppBackTarget? {
    return when {
        showBackupPanel -> InAppBackTarget.BackupPanel
        showArchivedPanel -> InAppBackTarget.ArchivedPanel
        isTaskPanelVisible -> InAppBackTarget.TaskPanel
        taskForDetail -> InAppBackTarget.TaskDetail
        hasActivePriority -> InAppBackTarget.QuadrantDetail
        else -> null
    }
}

internal val TaskPriority.color: Color
    get() = Color(colorInt())

internal val TaskPriority.title: String
    get() = label

internal val TaskPriority.shortTitle: String
    get() = shortLabel

internal val TaskPriority.type: QuadrantType
    get() = QuadrantType.valueOf(name)

internal val QuadrantType.priority: TaskPriority
    get() = TaskPriority.valueOf(name)

internal val TaskUiModel.completedSubTaskCount: Int
    get() = subTasks.count { it.completed }

internal val TaskUiModel.subTaskProgressLabel: String
    get() = "${completedSubTaskCount}/${subTasks.size} 子事项"

internal fun midnightMillis(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
}

internal fun minutesOfDay(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalTime()
    return time.hour * 60 + time.minute
}
