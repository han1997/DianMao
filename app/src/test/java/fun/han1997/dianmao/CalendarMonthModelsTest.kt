package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarMonthModelsTest {
    private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

    @Test
    fun monthGridStartsOnMondayAndPadsPreviousMonth() {
        // 2026-09-01 is a Tuesday, so the Monday-first grid leads with 2026-08-31.
        val grid = buildMonthGrid(YearMonth.of(2026, 9))

        assertEquals(LocalDate.of(2026, 8, 31), grid.first().date)
        assertEquals(DayOfWeek.MONDAY, grid.first().date.dayOfWeek)
        assertEquals(0, grid.size % 7)
        assertFalse(grid.first().inMonth)
    }

    @Test
    fun monthGridCoversEveryDayOfTheMonth() {
        val month = YearMonth.of(2026, 9)
        val grid = buildMonthGrid(month)
        val inMonthDates = grid.filter { it.inMonth }.map { it.date }

        assertEquals(month.lengthOfMonth(), inMonthDates.size)
        assertEquals(month.atDay(1), inMonthDates.first())
        assertEquals(month.atEndOfMonth(), inMonthDates.last())
    }

    @Test
    fun monthGridHasNoLeadingDaysWhenMonthStartsOnMonday() {
        // 2026-06-01 is a Monday.
        val grid = buildMonthGrid(YearMonth.of(2026, 6))

        assertEquals(LocalDate.of(2026, 6, 1), grid.first().date)
        assertTrue(grid.first().inMonth)
        assertEquals(0, grid.size % 7)
    }

    @Test
    fun dayMapGroupsTasksByLocalDueDate() {
        val now = millis(LocalDate.of(2026, 6, 19), hour = 12)

        val map = buildCalendarDayMap(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(LocalDate.of(2026, 6, 18), hour = 9)),
                task(id = 2, dueAtMillis = millis(LocalDate.of(2026, 6, 19), hour = 9)),
                task(id = 3, dueAtMillis = millis(LocalDate.of(2026, 6, 19), hour = 15))
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(1L), map.getValue(LocalDate.of(2026, 6, 18)).map { it.task.id })
        assertEquals(listOf(2L, 3L), map.getValue(LocalDate.of(2026, 6, 19)).map { it.task.id })
    }

    @Test
    fun dayMapSortsOpenTasksBeforeCompletedWithinADay() {
        val day = LocalDate.of(2026, 6, 19)
        val now = millis(day, hour = 12)

        val map = buildCalendarDayMap(
            tasks = listOf(
                task(id = 1, dueAtMillis = millis(day, hour = 18), completed = true),
                task(id = 2, dueAtMillis = millis(day, hour = 9))
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(listOf(2L, 1L), map.getValue(day).map { it.task.id })
    }

    @Test
    fun dayMapGroupsOpenRecurringTaskByCurrentCycle() {
        val now = millis(LocalDate.of(2026, 6, 19), hour = 12)
        // A daily task last due three days ago resolves to today's cycle.
        val map = buildCalendarDayMap(
            tasks = listOf(
                task(
                    id = 1,
                    dueAtMillis = millis(LocalDate.of(2026, 6, 16), hour = 9),
                    repeatRule = RepeatRule.Daily
                )
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertTrue(map.containsKey(LocalDate.of(2026, 6, 19)))
        assertFalse(map.containsKey(LocalDate.of(2026, 6, 16)))
    }

    @Test
    fun dayMapKeepsCompletedTaskOnItsStoredDate() {
        val now = millis(LocalDate.of(2026, 6, 19), hour = 12)
        val map = buildCalendarDayMap(
            tasks = listOf(
                task(
                    id = 1,
                    dueAtMillis = millis(LocalDate.of(2026, 6, 16), hour = 9),
                    repeatRule = RepeatRule.Daily,
                    completed = true
                )
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertTrue(map.containsKey(LocalDate.of(2026, 6, 16)))
        assertFalse(map.containsKey(LocalDate.of(2026, 6, 19)))
    }

    @Test
    fun dayMapExcludesUnscheduledAndArchivedTasks() {
        val now = millis(LocalDate.of(2026, 6, 19), hour = 12)
        val map = buildCalendarDayMap(
            tasks = listOf(
                task(id = 1, dueAtMillis = null),
                task(id = 2, dueAtMillis = millis(nowDate(now), hour = 12), archived = true),
                task(id = 3, dueAtMillis = millis(nowDate(now), hour = 12))
            ),
            nowMillis = now,
            zoneId = zoneId
        )

        assertEquals(1, map.size)
        assertEquals(listOf(3L), map.getValue(nowDate(now)).map { it.task.id })
    }

    private fun nowDate(nowMillis: Long): LocalDate =
        java.time.Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()

    private fun millis(date: LocalDate, hour: Int): Long {
        return date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun task(
        id: Long,
        dueAtMillis: Long?,
        repeatRule: RepeatRule = RepeatRule.None,
        completed: Boolean = false,
        archived: Boolean = false
    ): TaskUiModel {
        return TaskUiModel(
            id = id,
            title = "Task $id",
            quadrant = TaskPriority.ImportantUrgent,
            dueLabel = dueAtMillis?.toString(),
            dueAtMillis = dueAtMillis,
            useCalendar = false,
            calendarEventId = null,
            useAlarm = dueAtMillis != null,
            repeatRule = repeatRule,
            subTasks = emptyList(),
            completionCount = 0,
            lastCompletedAtMillis = null,
            completed = completed,
            completionNote = null,
            archived = archived
        )
    }
}
