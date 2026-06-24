package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatReminderCalculator
import `fun`.han1997.dianmao.data.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime
import java.time.ZoneId

class RepeatReminderCalculatorTest {
    private val zone = ZoneId.of("UTC")

    @Test
    fun dailyRepeatAdvancesPastMissedIntervals() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 15, 10, 0)

        val next = RepeatReminderCalculator.nextFutureDueAfter(current, RepeatRule.Daily, now, zone)

        assertEquals(millisAt(2026, 6, 16, 9, 0), next)
    }

    @Test
    fun weeklyRepeatAdvancesToNextFutureWeek() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 15, 8, 0)

        val next = RepeatReminderCalculator.nextFutureDueAfter(current, RepeatRule.Weekly, now, zone)

        assertEquals(millisAt(2026, 6, 15, 9, 0), next)
    }

    @Test
    fun nonRepeatingRuleDoesNotAdvance() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 15, 10, 0)

        val next = RepeatReminderCalculator.nextFutureDueAfter(current, RepeatRule.None, now, zone)

        assertEquals(current, next)
    }

    @Test
    fun currentCycleDueAtUsesTodayOccurrenceBeforeDailyReminderTime() {
        val current = millisAt(2026, 6, 1, 18, 0)
        val now = millisAt(2026, 6, 15, 10, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(current, RepeatRule.Daily, now, zone)

        assertEquals(millisAt(2026, 6, 15, 18, 0), dueAt)
    }

    @Test
    fun currentCycleDueAtUsesTodayOccurrenceAfterDailyReminderTime() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 15, 10, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(current, RepeatRule.Daily, now, zone)

        assertEquals(millisAt(2026, 6, 15, 9, 0), dueAt)
    }

    @Test
    fun currentCycleDueAtUsesCurrentIsoWeekForWeeklyRepeat() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 15, 8, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(current, RepeatRule.Weekly, now, zone)

        assertEquals(millisAt(2026, 6, 15, 9, 0), dueAt)
    }

    @Test
    fun currentCycleDueAtUsesCurrentMonthForMonthlyRepeat() {
        val current = millisAt(2026, 1, 31, 9, 0)
        val now = millisAt(2026, 3, 20, 10, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(current, RepeatRule.Monthly, now, zone)

        assertEquals(millisAt(2026, 3, 28, 9, 0), dueAt)
    }

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }
}
