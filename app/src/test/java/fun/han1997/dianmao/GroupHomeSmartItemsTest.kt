package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.HomeSmartTaskItem
import `fun`.han1997.dianmao.HomeSmartView
import `fun`.han1997.dianmao.groupHomeSmartItems
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
class GroupHomeSmartItemsTest {

    private val zoneId: ZoneId = ZoneId.of("Asia/Shanghai")

    private fun millis(date: LocalDate, hour: Int): Long {
        return date.atTime(hour, 0).atZone(zoneId).toInstant().toEpochMilli()
    }

    private fun task(id: Long, dueAtMillis: Long?): TaskUiModel {
        return TaskUiModel(
            id = id,
            title = "Task $id",
            quadrant = TaskPriority.ImportantUrgent,
            dueLabel = dueAtMillis?.toString(),
            dueAtMillis = dueAtMillis,
            useCalendar = false,
            calendarEventId = null,
            useAlarm = dueAtMillis != null,
            repeatRule = RepeatRule.None,
            subTasks = emptyList(),
            completionCount = 0,
            lastCompletedAtMillis = null,
            completed = false,
            completionNote = null,
            archived = false
        )
    }

    private fun item(id: Long, dueAtMillis: Long?): HomeSmartTaskItem {
        return HomeSmartTaskItem(
            task = task(id, dueAtMillis),
            dueAtMillis = dueAtMillis,
            dueLabel = dueAtMillis?.toString()
        )
    }

    @Test
    fun `today view splits overdue and today groups in order`() {
        // Now: 2026-09-07 12:00 local
        val now = millis(LocalDate.of(2026, 9, 7), 12)
        val items = listOf(
            item(1, millis(LocalDate.of(2026, 9, 7), 9)),
            item(2, millis(LocalDate.of(2026, 9, 6), 18)),
            item(3, millis(LocalDate.of(2026, 9, 7), 23))
        )

        val groups = groupHomeSmartItems(items, HomeSmartView.Today, now, zoneId)

        assertEquals(listOf("过期", "今天"), groups.map { it.label })
        assertEquals(listOf(2L), groups[0].items.map { it.task.id })
        assertEquals(listOf(1L, 3L), groups[1].items.map { it.task.id })
    }

    @Test
    fun `today view omits empty groups`() {
        val now = millis(LocalDate.of(2026, 9, 7), 12)
        val items = listOf(item(1, millis(LocalDate.of(2026, 9, 7), 9)))

        val groups = groupHomeSmartItems(items, HomeSmartView.Today, now, zoneId)

        assertEquals(listOf("今天"), groups.map { it.label })
    }

    @Test
    fun `upcoming view groups by day with relative labels`() {
        // Now: 2026-09-07 12:00 local (Monday)
        val now = millis(LocalDate.of(2026, 9, 7), 12)
        val items = listOf(
            item(1, millis(LocalDate.of(2026, 9, 9), 10)),
            item(2, millis(LocalDate.of(2026, 9, 8), 8)),
            item(3, millis(LocalDate.of(2026, 9, 8), 20))
        )

        val groups = groupHomeSmartItems(items, HomeSmartView.Upcoming, now, zoneId)

        assertEquals(2, groups.size)
        assertEquals("明天 · 周二", groups[0].label)
        assertEquals(listOf(2L, 3L), groups[0].items.map { it.task.id })
        assertEquals("后天 · 周三", groups[1].label)
        assertEquals(listOf(1L), groups[1].items.map { it.task.id })
    }

    @Test
    fun `upcoming view labels later days with month and day`() {
        val now = millis(LocalDate.of(2026, 9, 7), 12)
        val items = listOf(item(1, millis(LocalDate.of(2026, 9, 12), 10)))

        val groups = groupHomeSmartItems(items, HomeSmartView.Upcoming, now, zoneId)

        assertEquals("9月12日 · 周六", groups.single().label)
    }

    @Test
    fun `inbox and quadrants render one ungrouped section`() {
        val now = millis(LocalDate.of(2026, 9, 7), 12)
        val items = listOf(item(1, null), item(2, null))

        listOf(HomeSmartView.Inbox, HomeSmartView.Quadrants).forEach { view ->
            val groups = groupHomeSmartItems(items, view, now, zoneId)
            assertEquals(1, groups.size)
            assertEquals("", groups.single().label)
            assertEquals(2, groups.single().items.size)
        }
    }

    @Test
    fun `empty input yields no groups`() {
        val now = millis(LocalDate.of(2026, 9, 7), 12)

        HomeSmartView.entries.forEach { view ->
            assertTrue(groupHomeSmartItems(emptyList(), view, now, zoneId).isEmpty())
        }
    }
}
