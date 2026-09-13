package `fun`.han1997.dianmao.data

private val ISO_DAY_NAMES = mapOf(
    1 to "一",
    2 to "二",
    3 to "三",
    4 to "四",
    5 to "五",
    6 to "六",
    7 to "日"
)

private val WORKDAYS = setOf(1, 2, 3, 4, 5)
private val WEEKEND = setOf(6, 7)

/**
 * Human-readable repeat label covering the extended repeat model:
 * frequency + interval + selected weekdays, e.g. "每天", "每 2 天",
 * "工作日", "每周一、三", "每 2 周 · 周一、三", "每 3 个月", "每年".
 */
fun repeatRuleLabel(
    repeatRule: RepeatRule,
    repeatInterval: Int = 1,
    repeatWeekdays: Set<Int> = emptySet()
): String {
    val interval = repeatInterval.coerceAtLeast(1)
    val weekdays = repeatWeekdays.filter { it in 1..7 }.toSet()
    return when (repeatRule) {
        RepeatRule.None -> "不重复"
        RepeatRule.Count -> "计数"
        RepeatRule.Daily -> if (interval == 1) "每天" else "每 $interval 天"
        RepeatRule.Monthly -> if (interval == 1) "每月" else "每 $interval 个月"
        RepeatRule.Yearly -> if (interval == 1) "每年" else "每 $interval 年"
        RepeatRule.Weekly -> when {
            weekdays.isEmpty() -> if (interval == 1) "每周" else "每 $interval 周"
            weekdays == WORKDAYS -> if (interval == 1) "工作日" else "每 $interval 周 · 工作日"
            weekdays == WEEKEND -> if (interval == 1) "周末" else "每 $interval 周 · 周末"
            interval == 1 -> "每周" + weekdayListLabel(weekdays, withLeadingWeek = false)
            else -> "每 $interval 周 · " + weekdayListLabel(weekdays, withLeadingWeek = true)
        }
    }
}

private fun weekdayListLabel(weekdays: Set<Int>, withLeadingWeek: Boolean): String {
    return weekdays.sorted().mapIndexed { index, day ->
        val name = ISO_DAY_NAMES[day] ?: ""
        if (index == 0 && withLeadingWeek) "周$name" else name
    }.joinToString("、")
}
