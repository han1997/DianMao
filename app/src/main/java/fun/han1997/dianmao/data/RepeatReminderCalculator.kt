package `fun`.han1997.dianmao.data

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields

object RepeatReminderCalculator {
    fun nextDueAt(
        currentMillis: Long,
        repeatRule: RepeatRule,
        zoneId: ZoneId = ZoneId.systemDefault(),
        repeatInterval: Int = 1,
        repeatWeekdays: Set<Int> = emptySet()
    ): Long {
        val dateTime = Instant.ofEpochMilli(currentMillis)
            .atZone(zoneId)
            .toLocalDateTime()
        val interval = repeatInterval.coerceAtLeast(1)
        val next = when (repeatRule) {
            RepeatRule.None, RepeatRule.Count -> dateTime
            RepeatRule.Daily -> dateTime.plusDays(interval.toLong())
            RepeatRule.Weekly -> {
                val days = repeatWeekdays.filter { it in 1..7 }.toSet()
                if (days.isEmpty()) {
                    dateTime.plusWeeks(interval.toLong())
                } else {
                    nextWeekdayOccurrence(dateTime, interval, days)
                }
            }
            RepeatRule.Monthly -> dateTime.plusMonths(interval.toLong())
            RepeatRule.Yearly -> dateTime.plusYears(interval.toLong())
        }
        return next.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun nextFutureDueAfter(
        currentMillis: Long,
        repeatRule: RepeatRule,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        repeatInterval: Int = 1,
        repeatWeekdays: Set<Int> = emptySet()
    ): Long {
        if (repeatRule == RepeatRule.None) return currentMillis

        var next = nextDueAt(currentMillis, repeatRule, zoneId, repeatInterval, repeatWeekdays)
        while (next <= nowMillis) {
            next = nextDueAt(next, repeatRule, zoneId, repeatInterval, repeatWeekdays)
        }
        return next
    }

    fun currentCycleDueAt(
        currentMillis: Long,
        repeatRule: RepeatRule,
        nowMillis: Long,
        zoneId: ZoneId = ZoneId.systemDefault(),
        repeatInterval: Int = 1,
        repeatWeekdays: Set<Int> = emptySet()
    ): Long {
        if (!repeatRule.isCycleBased() || currentMillis > nowMillis) return currentMillis

        var current = currentMillis
        var next = nextDueAt(current, repeatRule, zoneId, repeatInterval, repeatWeekdays)
        while (next <= nowMillis) {
            current = next
            next = nextDueAt(current, repeatRule, zoneId, repeatInterval, repeatWeekdays)
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
            RepeatRule.Yearly -> first.year == second.year
            RepeatRule.None, RepeatRule.Count -> false
        }
    }

    /**
     * Earliest selected-weekday occurrence strictly after [dateTime], anchored
     * on the ISO week of the current occurrence and stepping [interval] weeks
     * per cycle (RFC 5545 BYDAY + INTERVAL semantics).
     */
    private fun nextWeekdayOccurrence(
        dateTime: LocalDateTime,
        interval: Int,
        weekdays: Set<Int>
    ): LocalDateTime {
        val currentDate = dateTime.toLocalDate()
        val anchorWeekStart = currentDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        var weekOffset = 0L
        while (true) {
            val weekStart = anchorWeekStart.plusWeeks(weekOffset)
            for (isoDay in 1..7) {
                if (isoDay !in weekdays) continue
                val candidate = weekStart.plusDays((isoDay - 1).toLong())
                if (candidate.isAfter(currentDate)) {
                    return candidate.atTime(dateTime.toLocalTime())
                }
            }
            weekOffset += interval
        }
    }
}
