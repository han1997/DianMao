package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.calendar.repeatRuleToRRule
import `fun`.han1997.dianmao.data.RepeatRule
import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatRuleRruleTest {
    @Test
    fun nonRepeatingRulesProduceNoRRule() {
        assertEquals(null, repeatRuleToRRule(RepeatRule.None))
        assertEquals(null, repeatRuleToRRule(RepeatRule.Count))
    }

    @Test
    fun plainFrequenciesProduceBaseRRule() {
        assertEquals("FREQ=DAILY", repeatRuleToRRule(RepeatRule.Daily))
        assertEquals("FREQ=WEEKLY", repeatRuleToRRule(RepeatRule.Weekly))
        assertEquals("FREQ=MONTHLY", repeatRuleToRRule(RepeatRule.Monthly))
        assertEquals("FREQ=YEARLY", repeatRuleToRRule(RepeatRule.Yearly))
    }

    @Test
    fun intervalGreaterThanOneAddsIntervalPart() {
        assertEquals("FREQ=DAILY;INTERVAL=3", repeatRuleToRRule(RepeatRule.Daily, repeatInterval = 3))
        assertEquals("FREQ=MONTHLY;INTERVAL=2", repeatRuleToRRule(RepeatRule.Monthly, repeatInterval = 2))
        assertEquals("FREQ=YEARLY;INTERVAL=2", repeatRuleToRRule(RepeatRule.Yearly, repeatInterval = 2))
    }

    @Test
    fun weeklyWeekdaysAddByDayInIsoOrder() {
        assertEquals(
            "FREQ=WEEKLY;BYDAY=MO,WE",
            repeatRuleToRRule(RepeatRule.Weekly, repeatWeekdays = setOf(1, 3))
        )
        assertEquals(
            "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
            repeatRuleToRRule(RepeatRule.Weekly, repeatWeekdays = setOf(1, 2, 3, 4, 5))
        )
        assertEquals(
            "FREQ=WEEKLY;BYDAY=SA,SU",
            repeatRuleToRRule(RepeatRule.Weekly, repeatWeekdays = setOf(6, 7))
        )
    }

    @Test
    fun weeklyIntervalAndWeekdaysCombine() {
        assertEquals(
            "FREQ=WEEKLY;INTERVAL=2;BYDAY=MO,WE",
            repeatRuleToRRule(RepeatRule.Weekly, repeatInterval = 2, repeatWeekdays = setOf(1, 3))
        )
    }

    @Test
    fun invalidWeekdaysAreDropped() {
        assertEquals("FREQ=WEEKLY", repeatRuleToRRule(RepeatRule.Weekly, repeatWeekdays = setOf(0, 9)))
    }
}
