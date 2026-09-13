package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.alerts.reminderRequestCode
import `fun`.han1997.dianmao.data.effectiveReminderLeadMinutes
import `fun`.han1997.dianmao.data.reminderLeadSetToStorage
import `fun`.han1997.dianmao.leadLabel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ReminderLeadStorageTest {
    @Test
    fun disabledLocalReminderProducesNoLeadTimes() {
        assertEquals(emptySet<Int>(), effectiveReminderLeadMinutes(false, 5, setOf(1440)))
        assertEquals(emptySet<Int>(), effectiveReminderLeadMinutes(false, 0, emptySet()))
    }

    @Test
    fun enabledLocalReminderCombinesPrimaryAndExtraLeadTimes() {
        assertEquals(
            setOf(5, 1440),
            effectiveReminderLeadMinutes(true, 5, setOf(1440))
        )
        assertEquals(
            setOf(0),
            effectiveReminderLeadMinutes(true, 0, emptySet())
        )
    }

    @Test
    fun negativeExtraLeadTimesAreDropped() {
        assertEquals(
            setOf(5, 30),
            effectiveReminderLeadMinutes(true, 5, setOf(-1, 30, -10))
        )
    }

    @Test
    fun leadSetRoundTripsThroughStorage() {
        val selections = listOf(
            setOf(0),
            setOf(5),
            setOf(5, 1440),
            setOf(0, 5, 30, 1440)
        )

        selections.forEach { selection ->
            val (primary, extras) = reminderLeadSetToStorage(selection)
            val restored = effectiveReminderLeadMinutes(true, primary, extras)
            assertEquals(selection, restored)
        }
    }

    @Test
    fun emptySelectionStoresOnTimeDefault() {
        val (primary, extras) = reminderLeadSetToStorage(emptySet())

        assertEquals(0, primary)
        assertEquals(emptySet<Int>(), extras)
    }

    @Test
    fun leadLabelJoinsSortedOptionLabels() {
        assertEquals("准时提醒", leadLabel(emptySet()))
        assertEquals("准时提醒", leadLabel(setOf(0)))
        assertEquals("提前 5 分钟", leadLabel(setOf(5)))
        assertEquals("准时提醒、提前 5 分钟", leadLabel(setOf(0, 5)))
        assertEquals("提前 5 分钟、提前 1 天", leadLabel(setOf(1440, 5)))
    }

    @Test
    fun requestCodesAreDistinctPerTaskAndLead() {
        val first = reminderRequestCode(1L, 5)
        val second = reminderRequestCode(1L, 1440)
        val third = reminderRequestCode(2L, 5)

        assertNotEquals(first, second)
        assertNotEquals(first, third)
        assertEquals(first, reminderRequestCode(1L, 5))
    }
}
