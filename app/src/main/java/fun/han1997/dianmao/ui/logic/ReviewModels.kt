package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.CompletionWithTask
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class ReviewCompletionEvent(
    val taskId: Long,
    val title: String,
    val quadrant: TaskPriority,
    val completedAtMillis: Long,
    val note: String?
)

internal data class ReviewDayCount(
    val date: LocalDate,
    val count: Int
)

internal data class ReviewStats(
    val last7DaysCount: Int,
    val last30DaysCount: Int,
    val dailyCounts: List<ReviewDayCount>,
    val quadrantCounts: Map<TaskPriority, Int>
)

internal const val REVIEW_EVENTS_LIMIT = 20

/**
 * Merges the two completion sources into one event stream: recurring-task
 * per-cycle completions from task_completions, plus one event per completed
 * non-recurring task (its last completion). Sorted newest first.
 */
internal fun buildReviewCompletionEvents(
    completions: List<CompletionWithTask>,
    tasks: List<TaskUiModel>
): List<ReviewCompletionEvent> {
    val events = completions.map { row ->
        ReviewCompletionEvent(
            taskId = row.taskId,
            title = row.taskTitle,
            quadrant = TaskPriority.valueOf(row.taskQuadrant.name),
            completedAtMillis = row.completedAtMillis,
            note = row.note
        )
    }.toMutableList()
    tasks.filter { it.completed && it.repeatRule == RepeatRule.None }.forEach { task ->
        val millis = task.lastCompletedAtMillis ?: return@forEach
        events += ReviewCompletionEvent(
            taskId = task.id,
            title = task.title,
            quadrant = task.quadrant,
            completedAtMillis = millis,
            note = task.completionNote
        )
    }
    return events.sortedByDescending { it.completedAtMillis }
}

/**
 * Aggregates the review stats over the completion events: 7/30-day totals,
 * the last 7 local days' distribution (oldest first), and quadrant counts
 * over the last 30 days.
 */
internal fun buildReviewStats(
    events: List<ReviewCompletionEvent>,
    nowMillis: Long,
    zoneId: ZoneId
): ReviewStats {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val day7Start = today.minusDays(6).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val day30Start = today.minusDays(29).atStartOfDay(zoneId).toInstant().toEpochMilli()

    val last7 = events.count { it.completedAtMillis >= day7Start }
    val last30 = events.count { it.completedAtMillis >= day30Start }

    val dailyCounts = (6 downTo 0).map { offset ->
        val date = today.minusDays(offset.toLong())
        ReviewDayCount(
            date = date,
            count = events.count { event ->
                Instant.ofEpochMilli(event.completedAtMillis).atZone(zoneId).toLocalDate() == date
            }
        )
    }

    val quadrantCounts = TaskPriority.entries.associateWith { quadrant ->
        events.count { it.completedAtMillis >= day30Start && it.quadrant == quadrant }
    }

    return ReviewStats(
        last7DaysCount = last7,
        last30DaysCount = last30,
        dailyCounts = dailyCounts,
        quadrantCounts = quadrantCounts
    )
}
