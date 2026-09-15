package `fun`.han1997.dianmao.alerts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderActionRequestCodeTest {

    @Test
    fun sameInputsProduceSameCode() {
        val first = reminderActionRequestCode(42L, 5, ReminderActionKind.Complete)
        val second = reminderActionRequestCode(42L, 5, ReminderActionKind.Complete)
        assertEquals(first, second)
    }

    @Test
    fun differentActionsProduceDifferentCodes() {
        val complete = reminderActionRequestCode(42L, 5, ReminderActionKind.Complete)
        val snooze = reminderActionRequestCode(42L, 5, ReminderActionKind.Snooze)
        assertTrue(complete != snooze)
    }

    @Test
    fun actionCodesStayDistinctFromAlarmCodes() {
        val alarmCodes = mutableSetOf<Int>()
        for (taskId in 1L..200L) {
            for (lead in 0..1440 step 5) {
                alarmCodes.add(reminderRequestCode(taskId, lead))
            }
        }
        for (taskId in 1L..200L) {
            for (lead in 0..1440 step 5) {
                for (kind in ReminderActionKind.entries) {
                    assertTrue(reminderActionRequestCode(taskId, lead, kind) !in alarmCodes)
                }
            }
        }
    }

    @Test
    fun snoozeDelayIsTenMinutes() {
        assertEquals(600_000L, ReminderSnooze.DELAY_MILLIS)
        assertEquals(1_000_000L, ReminderSnooze.triggerAtMillis(400_000L))
    }
}
