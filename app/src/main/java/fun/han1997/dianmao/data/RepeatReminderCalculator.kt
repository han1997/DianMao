package `fun`.han1997.dianmao.data

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.WeekFields

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
            RepeatRule.None, RepeatRule.Count -> dateTime
            RepeatRule.Daily -> dateTime.plusDays(1)
            RepeatRule.Weekly -> dateTime.plusWeeks(1)
            RepeatRule.Monthly -> dateTime.plusMonths(1)
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

    fun currentCycleDueAt(
        currentMillis: Long,
        repeatRule: RepeatRule,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): Long {
        if (!repeatRule.isCycleBased() || currentMillis > nowMillis) return currentMillis

        var current = currentMillis
        var next = nextDueAt(current, repeatRule, zoneId)
        while (next <= nowMillis) {
            current = next
            next = nextDueAt(current, repeatRule, zoneId)
        }

        return when {
            isInSameRepeatCycle(next, nowMillis, repeatRule, zoneId) -> next
            isInSameRepeatCycle(current, nowMillis, repeatRule, zoneId) -> current
            else -> current
        }
    }

    private fun isInSameRepeatCycle(
        firstMillis: Long,
        secondMillis: Long,
        repeatRule: RepeatRule,
        zoneId: ZoneId
    ): Boolean {
        val first = Instant.ofEpochMilli(firstMillis).atZone(zoneId).toLocalDate()
        val second = Instant.ofEpochMilli(secondMillis).atZone(zoneId).toLocalDate()
        return when (repeatRule) {
            RepeatRule.Daily -> first.isEqual(second)
            RepeatRule.Weekly -> {
                val weekFields = WeekFields.ISO
                first.get(weekFields.weekBasedYear()) == second.get(weekFields.weekBasedYear()) &&
                    first.get(weekFields.weekOfWeekBasedYear()) == second.get(weekFields.weekOfWeekBasedYear())
            }
            RepeatRule.Monthly -> first.year == second.year && first.monthValue == second.monthValue
            RepeatRule.None, RepeatRule.Count -> false
        }
    }
}
