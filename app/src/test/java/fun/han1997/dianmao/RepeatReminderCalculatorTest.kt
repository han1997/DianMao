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

    @Test
    fun yearlyRepeatAdvancesOneYear() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val next = RepeatReminderCalculator.nextDueAt(current, RepeatRule.Yearly, zone)

        assertEquals(millisAt(2027, 6, 1, 9, 0), next)
    }

    @Test
    fun yearlyRepeatOnFeb29ClampsToFeb28() {
        val current = millisAt(2028, 2, 29, 9, 0)
        val next = RepeatReminderCalculator.nextDueAt(current, RepeatRule.Yearly, zone)

        assertEquals(millisAt(2029, 2, 28, 9, 0), next)
    }

    @Test
    fun intervalRepeatAdvancesByNPeriods() {
        val current = millisAt(2026, 6, 1, 9, 0)

        val dailyEvery3 = RepeatReminderCalculator.nextDueAt(current, RepeatRule.Daily, zone, repeatInterval = 3)
        val weeklyEvery2 = RepeatReminderCalculator.nextDueAt(current, RepeatRule.Weekly, zone, repeatInterval = 2)

        assertEquals(millisAt(2026, 6, 4, 9, 0), dailyEvery3)
        assertEquals(millisAt(2026, 6, 15, 9, 0), weeklyEvery2)
    }

    @Test
    fun weeklyWeekdaysFindNextSelectedDayInSameWeek() {
        val current = millisAt(2026, 6, 1, 9, 0)

        val next = RepeatReminderCalculator.nextDueAt(
            current, RepeatRule.Weekly, zone,
            repeatWeekdays = setOf(1, 3)
        )

        assertEquals(millisAt(2026, 6, 3, 9, 0), next)
    }

    @Test
    fun weeklyWeekdaysWrapToNextWeek() {
        val current = millisAt(2026, 6, 5, 9, 0)

        val next = RepeatReminderCalculator.nextDueAt(
            current, RepeatRule.Weekly, zone,
            repeatWeekdays = setOf(1)
        )

        assertEquals(millisAt(2026, 6, 8, 9, 0), next)
    }

    @Test
    fun weeklyInterval2WeekdaysAnchorOnStartWeek() {
        val current = millisAt(2026, 6, 1, 9, 0)

        val firstHop = RepeatReminderCalculator.nextDueAt(
            current, RepeatRule.Weekly, zone,
            repeatInterval = 2,
            repeatWeekdays = setOf(1, 3)
        )
        val secondHop = RepeatReminderCalculator.nextDueAt(
            firstHop, RepeatRule.Weekly, zone,
            repeatInterval = 2,
            repeatWeekdays = setOf(1, 3)
        )

        assertEquals(millisAt(2026, 6, 3, 9, 0), firstHop)
        assertEquals(millisAt(2026, 6, 15, 9, 0), secondHop)
    }

    @Test
    fun weeklyWeekdaysAdvancePastMissedOccurrences() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 10, 10, 0)

        val next = RepeatReminderCalculator.nextFutureDueAfter(
            current, RepeatRule.Weekly, now, zone,
            repeatWeekdays = setOf(1)
        )

        assertEquals(millisAt(2026, 6, 15, 9, 0), next)
    }

    @Test
    fun currentCycleDueAtWeekdaysShowsOverdueOccurrenceInCurrentWeek() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 4, 10, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(
            current, RepeatRule.Weekly, now, zone,
            repeatWeekdays = setOf(1, 3)
        )

        assertEquals(millisAt(2026, 6, 3, 9, 0), dueAt)
    }

    @Test
    fun currentCycleDueAtWeekdaysShowsUpcomingOccurrenceInCurrentWeek() {
        val current = millisAt(2026, 6, 1, 9, 0)
        val now = millisAt(2026, 6, 2, 10, 0)

        val dueAt = RepeatReminderCalculator.currentCycleDueAt(
            current, RepeatRule.Weekly, now, zone,
            repeatWeekdays = setOf(1, 3)
        )

        assertEquals(millisAt(2026, 6, 3, 9, 0), dueAt)
    }

    private fun millisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long {
        return LocalDateTime.of(year, month, day, hour, minute)
            .atZone(zone)
            .toInstant()
            .toEpochMilli()
    }
}
