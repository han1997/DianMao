package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.BackupCodecRules
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.quadrantTypeOrNull
import `fun`.han1997.dianmao.data.repeatRuleOrNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Covers the data-safety decisions of a backup import without touching
 * Android's `org.json` stub: the format/version gate and the sanitizers that
 * narrow untrusted payload fields.
 */
class BackupCodecRulesTest {

    // --- format/version gate -------------------------------------------------

    @Test
    fun acceptsCurrentDianMaoPayload() {
        assertTrue(BackupCodecRules.acceptsPayload("dianmao.tasks", 7))
    }

    @Test
    fun acceptsEverySupportedPastVersion() {
        for (version in 1..BackupCodecRules.SUPPORTED_VERSION) {
            assertTrue("version $version", BackupCodecRules.acceptsPayload("dianmao.tasks", version))
        }
    }

    @Test
    fun rejectsFutureVersion() {
        assertFalse(BackupCodecRules.acceptsPayload("dianmao.tasks", BackupCodecRules.SUPPORTED_VERSION + 1))
    }

    @Test
    fun rejectsForeignFormat() {
        assertFalse(BackupCodecRules.acceptsPayload("other.app.tasks", 7))
    }

    @Test
    fun rejectsMissingFormatOrVersion() {
        assertFalse(BackupCodecRules.acceptsPayload(null, 7))
        assertFalse(BackupCodecRules.acceptsPayload("dianmao.tasks", null))
    }

    @Test
    fun rejectsNonPositiveVersion() {
        assertFalse(BackupCodecRules.acceptsPayload("dianmao.tasks", 0))
        assertFalse(BackupCodecRules.acceptsPayload("dianmao.tasks", -3))
    }

    // --- field sanitizers ----------------------------------------------------

    @Test
    fun repeatWeekdaysKeepOnlyIsoRange() {
        assertEquals(setOf(1, 5, 7), BackupCodecRules.sanitizeRepeatWeekdays(listOf(0, 1, 5, 7, 8, -2)))
    }

    @Test
    fun repeatWeekdaysOfEmptyInputIsEmpty() {
        assertTrue(BackupCodecRules.sanitizeRepeatWeekdays(emptyList()).isEmpty())
    }

    @Test
    fun reminderLeadExtrasDropNegatives() {
        assertEquals(setOf(0, 5, 30, 1440), BackupCodecRules.sanitizeReminderLeadExtras(listOf(-1, 0, 5, 30, 1440)))
    }

    // --- enum decoding (Room TypeConverters) ---------------------------------

    @Test
    fun quadrantTypeDecodesKnownName() {
        assertEquals("ImportantUrgent", quadrantTypeOrNull("ImportantUrgent")?.name)
    }

    @Test
    fun quadrantTypeReturnsNullForUnknownInsteadOfThrowing() {
        assertNull(quadrantTypeOrNull("Bogus"))
        assertNull(quadrantTypeOrNull(""))
    }

    @Test
    fun repeatRuleDecodesKnownName() {
        assertEquals(RepeatRule.Weekly, repeatRuleOrNull("Weekly"))
    }

    @Test
    fun repeatRuleReturnsNullForUnknownInsteadOfThrowing() {
        assertNull(repeatRuleOrNull("Hourly"))
    }
}