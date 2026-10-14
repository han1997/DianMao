package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.shouldAdvanceRepeatAfterReminder
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the reminder-fired cycle advance against a task that carries several
 * lead-time alarms for one occurrence: only the first firing lead may roll the
 * task to its next cycle.
 */
class RepeatReminderAdvanceGuardTest {

    private val due = 1_700_000_000_000L

    @Test
    fun legacyIntentWithoutExpectedDueAlwaysAdvances() {
        assertTrue(shouldAdvanceRepeatAfterReminder(expectedDueAt = null, currentDueAt = due))
    }

    @Test
    fun firstLeadStillMatchingPersistedDueAdvances() {
        assertTrue(shouldAdvanceRepeatAfterReminder(expectedDueAt = due, currentDueAt = due))
    }

    @Test
    fun laterLeadSeesAdvancedDueAndSkips() {
        val advanced = due + 86_400_000L
        assertFalse(shouldAdvanceRepeatAfterReminder(expectedDueAt = due, currentDueAt = advanced))
    }

    @Test
    fun snoozeReusingAFutureOccurrenceDoesNotAdvanceItAgain() {
        val nextOccurrence = due + 7 * 86_400_000L
        assertFalse(shouldAdvanceRepeatAfterReminder(expectedDueAt = due, currentDueAt = nextOccurrence))
    }
}