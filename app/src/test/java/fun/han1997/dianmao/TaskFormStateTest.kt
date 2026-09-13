package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.QuadrantType
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskFormState
import `fun`.han1997.dianmao.data.normalizedTimeSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskFormStateTest {
    @Test
    fun clearingDateClearsAllTimeDependentSettings() {
        val normalized = TaskFormState(
            quadrant = QuadrantType.ImportantUrgent,
            dueDateMillis = null,
            dueTimeMinutes = 600,
            useCalendar = true,
            useAlarm = true,
            repeatRule = RepeatRule.Daily
        ).normalizedTimeSettings()

        assertNull(normalized.dueTimeMinutes)
        assertFalse(normalized.useCalendar)
        assertFalse(normalized.useAlarm)
        assertEquals(RepeatRule.None, normalized.repeatRule)
    }

    @Test
    fun repeatRuleRequiresOnlyADueDate() {
        val withoutAlarm = TaskFormState(
            dueDateMillis = 1_000L,
            useCalendar = true,
            useAlarm = false,
            repeatRule = RepeatRule.Weekly
        ).normalizedTimeSettings()

        assertTrue(withoutAlarm.useCalendar)
        assertFalse(withoutAlarm.useAlarm)
        assertEquals(RepeatRule.Weekly, withoutAlarm.repeatRule)
    }

    @Test
    fun repeatWithoutDateIsCleared() {
        val withoutDate = TaskFormState(
            dueDateMillis = null,
            useAlarm = false,
            repeatRule = RepeatRule.Yearly,
            repeatInterval = 2
        ).normalizedTimeSettings()

        assertEquals(RepeatRule.None, withoutDate.repeatRule)
        assertEquals(1, withoutDate.repeatInterval)
        assertTrue(withoutDate.repeatWeekdays.isEmpty())
    }

    @Test
    fun datedAlarmTaskPreservesRepeatRule() {
        val normalized = TaskFormState(
            dueDateMillis = 1_000L,
            dueTimeMinutes = 540,
            useAlarm = true,
            repeatRule = RepeatRule.Monthly
        ).normalizedTimeSettings()

        assertEquals(RepeatRule.Monthly, normalized.repeatRule)
        assertTrue(normalized.useAlarm)
        assertEquals(540, normalized.dueTimeMinutes)
    }

    @Test
    fun repeatIntervalIsCoercedToOne() {
        val normalized = TaskFormState(
            dueDateMillis = 1_000L,
            repeatRule = RepeatRule.Daily,
            repeatInterval = 0
        ).normalizedTimeSettings()

        assertEquals(1, normalized.repeatInterval)
    }

    @Test
    fun repeatWeekdaysAreOnlyKeptForWeeklyRules() {
        val weekly = TaskFormState(
            dueDateMillis = 1_000L,
            repeatRule = RepeatRule.Weekly,
            repeatWeekdays = setOf(1, 3, 9, 0)
        ).normalizedTimeSettings()

        assertEquals(setOf(1, 3), weekly.repeatWeekdays)

        val daily = TaskFormState(
            dueDateMillis = 1_000L,
            repeatRule = RepeatRule.Daily,
            repeatWeekdays = setOf(1, 3)
        ).normalizedTimeSettings()

        assertTrue(daily.repeatWeekdays.isEmpty())
    }

    @Test
    fun reminderLeadMinutesAreOnlyKeptForEnabledLocalReminder() {
        val disabled = TaskFormState(
            dueDateMillis = 1_000L,
            useLocalReminder = false,
            reminderLeadMinutes = setOf(5, 1440)
        ).normalizedTimeSettings()

        assertTrue(disabled.reminderLeadMinutes.isEmpty())

        val enabled = TaskFormState(
            dueDateMillis = 1_000L,
            useLocalReminder = true,
            reminderLeadMinutes = setOf(5, 1440, -1)
        ).normalizedTimeSettings()

        assertEquals(setOf(5, 1440), enabled.reminderLeadMinutes)
    }
}
