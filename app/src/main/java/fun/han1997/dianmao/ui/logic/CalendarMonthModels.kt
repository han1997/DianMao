package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskUiModel
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

internal data class MonthDayCell(
    val date: LocalDate,
    val inMonth: Boolean
)

/**
 * Monday-first 7-column month grid. Leading and trailing adjacent-month days
 * pad the first and last weeks so every rendered row is a full week. Pure
 * projection over [month]; unit tested.
 */
internal fun buildMonthGrid(
    month: YearMonth,
    firstDayOfWeek: DayOfWeek = DayOfWeek.MONDAY
): List<MonthDayCell> {
    val first = month.atDay(1)
    val leading = (first.dayOfWeek.value - firstDayOfWeek.value + 7) % 7
    val gridStart = first.minusDays(leading.toLong())
    val cells = (leading + month.lengthOfMonth() + 6) / 7 * 7
    return (0 until cells).map { offset ->
        val date = gridStart.plusDays(offset.toLong())
        MonthDayCell(date = date, inMonth = YearMonth.from(date) == month)
    }
}

/**
 * Groups active tasks by the local date they are due on. Open tasks resolve
 * recurring cycles through [effectiveDueAtMillis] so they land on the current
 * cycle day; completed tasks keep their stored due date. Archived and
 * untimed tasks are omitted. Within a day, open tasks sort before completed
 * ones, then by due time and id. Pure projection; unit tested.
 */
internal fun buildCalendarDayMap(
    tasks: List<TaskUiModel>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): Map<LocalDate, List<HomeSmartTaskItem>> {
    return tasks.mapNotNull { task ->
        if (task.archived) return@mapNotNull null
        val dueAt = if (task.completed) task.dueAtMillis else effectiveDueAtMillis(task, nowMillis, zoneId)
        dueAt ?: return@mapNotNull null
        val date = Instant.ofEpochMilli(dueAt).atZone(zoneId).toLocalDate()
        date to HomeSmartTaskItem(
            task = task,
            dueAtMillis = dueAt,
            dueLabel = task.displayDueLabel(dueAt, zoneId)
        )
    }.groupBy({ it.first }, { it.second })
        .mapValues { (_, items) ->
            items.sortedWith(
                compareBy(
                    { it.task.completed },
                    { it.dueAtMillis ?: Long.MAX_VALUE },
                    { it.task.id }
                )
            )
        }
}
