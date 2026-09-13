package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.CompletionWithTask
import `fun`.han1997.dianmao.data.QuadrantType
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class ReviewModelsTest {
    private val zone = ZoneId.of("UTC")

    private fun task(
        id: Long,
        title: String,
        quadrant: TaskPriority = TaskPriority.ImportantUrgent,
        completed: Boolean = false,
        lastCompletedAtMillis: Long? = null,
        note: String? = null,
        repeatRule: RepeatRule = RepeatRule.None
    ): TaskUiModel {
        return TaskUiModel(
            id = id,
            title = title,
            quadrant = quadrant,
            dueLabel = null,
            dueAtMillis = null,
            useCalendar = false,
            calendarEventId = null,
            useAlarm = false,
            repeatRule = repeatRule,
            subTasks = emptyList(),
            completionCount = if (completed) 1 else 0,
            lastCompletedAtMillis = lastCompletedAtMillis,
            completed = completed,
            completionNote = note
        )
    }

    private fun completion(
        taskId: Long,
        title: String,
        atMillis: Long,
        note: String? = null,
        quadrant: QuadrantType = QuadrantType.ImportantUrgent
    ): CompletionWithTask {
        return CompletionWithTask(
            taskId = taskId,
            completedAtMillis = atMillis,
            note = note,
            taskTitle = title,
            taskQuadrant = quadrant
        )
    }

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int = 12): Long {
        return LocalDateTime.of(year, month, day, hour, 0).atZone(zone).toInstant().toEpochMilli()
    }

    @Test
    fun recurringCompletionsMergeWithNonRecurringTaskCompletions() {
        val completions = listOf(
            completion(1L, "周报", millisAt(2026, 9, 13), note = "顺利")
        )
        val tasks = listOf(
            task(2L, "买书", completed = true, lastCompletedAtMillis = millisAt(2026, 9, 12), note = "打折入手"),
            task(3L, "未完成项"),
            task(4L, "每日打卡", completed = true, lastCompletedAtMillis = millisAt(2026, 9, 11), repeatRule = RepeatRule.Daily)
        )

        val events = buildReviewCompletionEvents(completions, tasks)

        assertEquals(2, events.size)
        assertEquals("周报", events[0].title)
        assertEquals("买书", events[1].title)
        assertEquals("顺利", events[0].note)
        assertEquals("打折入手", events[1].note)
    }

    @Test
    fun eventsSortNewestFirst() {
        val completions = listOf(
            completion(1L, "早的", millisAt(2026, 9, 10)),
            completion(2L, "晚的", millisAt(2026, 9, 13))
        )

        val events = buildReviewCompletionEvents(completions, emptyList())

        assertEquals("晚的", events[0].title)
        assertEquals("早的", events[1].title)
    }

    @Test
    fun statsCountWithinLocalDayWindows() {
        val today = millisAt(2026, 9, 13)
        val yesterday = millisAt(2026, 9, 12)
        val tenDaysAgo = millisAt(2026, 9, 3)
        val events = listOf(
            ReviewCompletionEvent(1L, "a", TaskPriority.ImportantUrgent, today, null),
            ReviewCompletionEvent(2L, "b", TaskPriority.ImportantNotUrgent, yesterday, null),
            ReviewCompletionEvent(3L, "c", TaskPriority.NotImportantUrgent, tenDaysAgo, null)
        )

        val stats = buildReviewStats(events, today, zone)

        assertEquals(2, stats.last7DaysCount)
        assertEquals(3, stats.last30DaysCount)
        assertEquals(7, stats.dailyCounts.size)
        assertEquals(1, stats.dailyCounts.last().count)
        assertEquals(1, stats.dailyCounts[stats.dailyCounts.size - 2].count)
        assertEquals(1, stats.quadrantCounts[TaskPriority.ImportantUrgent])
        assertEquals(1, stats.quadrantCounts[TaskPriority.ImportantNotUrgent])
        assertEquals(0, stats.quadrantCounts[TaskPriority.NotImportantNotUrgent])
    }

    @Test
    fun emptyEventsProduceZeroedStats() {
        val stats = buildReviewStats(emptyList(), millisAt(2026, 9, 13), zone)

        assertEquals(0, stats.last7DaysCount)
        assertEquals(0, stats.last30DaysCount)
        assertEquals(7, stats.dailyCounts.size)
        assertEquals(0, stats.quadrantCounts.values.sum())
    }
}
