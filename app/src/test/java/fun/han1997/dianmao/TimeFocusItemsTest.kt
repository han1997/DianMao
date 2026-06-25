package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class TimeFocusItemsTest {
    private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun ordersOverdueBeforeTodayBeforeUpcoming() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildTimeFocusItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(today.plusDays(1), hour = 9)),
                task(id = 2, dueAtMillis = millis(today, hour = 8)),
                task(id = 3, dueAtMillis = millis(today, hour = 18)),
                task(id = 4, dueAtMillis = millis(today.minusDays(1), hour = 20))
            ),
            nowMillis = now,
            zoneId = zoneId,
            limit = 4
        )

        assertEquals(listOf(4L, 2L, 3L, 1L), items.map { it.task.id })
        assertEquals(
            listOf(TimeFocusKind.Overdue, TimeFocusKind.Overdue, TimeFocusKind.Today, TimeFocusKind.Upcoming),
            items.map { it.kind }
        )
    }

    @Test
    fun excludesCompletedArchivedAndUnscheduledTasks() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildTimeFocusItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = null),
                task(id = 2, dueAtMillis = millis(today, hour = 11), completed = true),
                task(id = 3, dueAtMillis = millis(today, hour = 12), archived = true),
                task(id = 4, dueAtMillis = millis(today, hour = 13))
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(4L), items.map { it.task.id })
    }

    @Test
    fun respectsLimitAfterSorting() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildTimeFocusItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(today.plusDays(3), hour = 9)),
                task(id = 2, dueAtMillis = millis(today, hour = 16)),
                task(id = 3, dueAtMillis = millis(today.minusDays(1), hour = 9)),
                task(id = 4, dueAtMillis = millis(today.plusDays(1), hour = 9))
            ),
            nowMillis = now,
            zoneId = zoneId,
            limit = 2
        )

        assertEquals(listOf(3L, 2L), items.map { it.task.id })
    }

    @Test
    fun dailyRepeatUsesTodayOccurrenceWhenReminderIsLaterToday() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)
        val originalDueAt = millis(today.minusDays(3), hour = 18)

        val items = buildTimeFocusItems(
            tasks = listOf(
                task(
                    id = 1,
                    dueAtMillis = originalDueAt,
                    repeatRule = RepeatRule.Daily,
                    dueLabel = "stale"
                )
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(TimeFocusKind.Today, items.single().kind)
        assertEquals(millis(today, hour = 18), items.single().dueAtMillis)
        assertNotEquals("stale", items.single().dueLabel)
    }

    @Test
    fun dailyRepeatUsesTodayOccurrenceWhenReminderIsAlreadyPast() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)
        val originalDueAt = millis(today.minusDays(3), hour = 8)

        val items = buildTimeFocusItems(
            tasks = listOf(
                task(
                    id = 1,
                    dueAtMillis = originalDueAt,
                    repeatRule = RepeatRule.Daily,
                    dueLabel = "stale"
                )
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(TimeFocusKind.Overdue, items.single().kind)
        assertEquals(millis(today, hour = 8), items.single().dueAtMillis)
        assertNotEquals("stale", items.single().dueLabel)
    }

    @Test
    fun todaySmartViewIncludesOverdueAndTodayTimedTasks() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildHomeSmartTaskItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(today.minusDays(1), hour = 18)),
                task(id = 2, dueAtMillis = millis(today, hour = 8)),
                task(id = 3, dueAtMillis = millis(today, hour = 18)),
                task(id = 4, dueAtMillis = millis(today.plusDays(1), hour = 9)),
                task(id = 5, dueAtMillis = null),
                task(id = 6, dueAtMillis = millis(today, hour = 12), completed = true)
            ),
            view = HomeSmartView.Today,
            query = "",
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(1L, 2L, 3L), items.map { it.task.id })
    }

    @Test
    fun upcomingSmartViewShowsNextSevenLocalDaysOnly() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildHomeSmartTaskItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(today, hour = 18)),
                task(id = 2, dueAtMillis = millis(today.plusDays(1), hour = 9)),
                task(id = 3, dueAtMillis = millis(today.plusDays(6), hour = 9)),
                task(id = 4, dueAtMillis = millis(today.plusDays(7), hour = 9)),
                task(id = 5, dueAtMillis = null)
            ),
            view = HomeSmartView.Upcoming,
            query = "",
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(2L, 3L), items.map { it.task.id })
    }

    @Test
    fun inboxSmartViewShowsUnscheduledOpenTasks() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildHomeSmartTaskItems(
            tasks = listOf(
                task(id = 1, dueAtMillis = null),
                task(id = 2, dueAtMillis = millis(today, hour = 11)),
                task(id = 3, dueAtMillis = null, completed = true),
                task(id = 4, dueAtMillis = null, archived = true)
            ),
            view = HomeSmartView.Inbox,
            query = "",
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(1L), items.map { it.task.id })
    }

    @Test
    fun smartViewSearchMatchesTaskAndSubTaskTitles() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildHomeSmartTaskItems(
            tasks = listOf(
                task(id = 1, title = "Confirm supplier", dueAtMillis = null),
                task(
                    id = 2,
                    title = "Prepare deck",
                    dueAtMillis = null,
                    subTasks = listOf(TaskSubTask(id = 1, title = "supplier numbers"))
                ),
                task(id = 3, title = "Review notes", dueAtMillis = null)
            ),
            view = HomeSmartView.Inbox,
            query = "supplier",
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(1L, 2L), items.map { it.task.id })
    }

    @Test
    fun smartViewUsesCurrentCycleDueLabelForRepeatingTasks() {
        val today = LocalDate.of(2026, 6, 19)
        val now = millis(today, hour = 10)

        val items = buildHomeSmartTaskItems(
            tasks = listOf(
                task(
                    id = 1,
                    dueAtMillis = millis(today.minusDays(3), hour = 18),
                    repeatRule = RepeatRule.Daily,
                    dueLabel = "stale"
                )
            ),
            view = HomeSmartView.Today,
            query = "",
            nowMillis = now,
            zoneId = zoneId
        )

        assertNotEquals("stale", items.single().dueLabel)
    }

    private fun millis(date: LocalDate, hour: Int): Long {
        return date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun task(
        id: Long,
        title: String = "Task $id",
        dueAtMillis: Long?,
        repeatRule: RepeatRule = RepeatRule.None,
        dueLabel: String? = dueAtMillis?.toString(),
        subTasks: List<TaskSubTask> = emptyList(),
        completed: Boolean = false,
        archived: Boolean = false
    ): TaskUiModel {
        return TaskUiModel(
            id = id,
            title = title,
            quadrant = TaskPriority.ImportantUrgent,
            dueLabel = dueLabel,
            dueAtMillis = dueAtMillis,
            useCalendar = false,
            calendarEventId = null,
            useAlarm = dueAtMillis != null,
            repeatRule = repeatRule,
            subTasks = subTasks,
            completionCount = 0,
            lastCompletedAtMillis = null,
            completed = completed,
            completionNote = null,
            archived = archived
        )
    }
}
