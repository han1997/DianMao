package `fun`.han1997.dianmao.data

import java.time.Instant
import java.time.ZoneId

object RepeatReminderCalculator {
    fun nextDueAt(
        currentMillis: Long,
        repeatRule: RepeatRule,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        val dateTime = Instant.ofEpochMilli(currentMillis)
            .atZone(zoneId)
            .toLocalDateTime()
        val next = when (repeatRule) {
            RepeatRule.None -> dateTime
            RepeatRule.Daily, RepeatRule.DailyCount -> dateTime.plusDays(1)
            RepeatRule.Weekly, RepeatRule.WeeklyCount -> dateTime.plusWeeks(1)
            RepeatRule.Monthly, RepeatRule.MonthlyCount -> dateTime.plusMonths(1)
        }
        return next.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun nextFutureDueAfter(
        currentMillis: Long,
        repeatRule: RepeatRule,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        if (repeatRule == RepeatRule.None) return currentMillis

        var next = nextDueAt(currentMillis, repeatRule, zoneId)
        while (next <= nowMillis) {
            next = nextDueAt(next, repeatRule, zoneId)
        }
        return next
    }
}
