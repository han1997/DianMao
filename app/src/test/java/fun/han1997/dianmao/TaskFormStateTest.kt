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
    fun repeatRuleRequiresDateAndAlarm() {
        val withoutAlarm = TaskFormState(
            dueDateMillis = 1_000L,
            useCalendar = true,
            useAlarm = false,
            repeatRule = RepeatRule.Weekly
        ).normalizedTimeSettings()

        assertTrue(withoutAlarm.useCalendar)
        assertFalse(withoutAlarm.useAlarm)
        assertEquals(RepeatRule.None, withoutAlarm.repeatRule)
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
}
