package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.repeatRuleLabel
import org.junit.Assert.assertEquals
import org.junit.Test

class RepeatRuleLabelTest {
    @Test
    fun plainRulesKeepTheirLabels() {
        assertEquals("不重复", repeatRuleLabel(RepeatRule.None))
        assertEquals("计数", repeatRuleLabel(RepeatRule.Count))
        assertEquals("每天", repeatRuleLabel(RepeatRule.Daily))
        assertEquals("每周", repeatRuleLabel(RepeatRule.Weekly))
        assertEquals("每月", repeatRuleLabel(RepeatRule.Monthly))
        assertEquals("每年", repeatRuleLabel(RepeatRule.Yearly))
    }

    @Test
    fun intervalRulesIncludeThePeriodCount() {
        assertEquals("每 3 天", repeatRuleLabel(RepeatRule.Daily, repeatInterval = 3))
        assertEquals("每 2 周", repeatRuleLabel(RepeatRule.Weekly, repeatInterval = 2))
        assertEquals("每 3 个月", repeatRuleLabel(RepeatRule.Monthly, repeatInterval = 3))
        assertEquals("每 2 年", repeatRuleLabel(RepeatRule.Yearly, repeatInterval = 2))
    }

    @Test
    fun weekdaySelectionsRenderAsDayLists() {
        assertEquals("每周一、三", repeatRuleLabel(RepeatRule.Weekly, repeatWeekdays = setOf(1, 3)))
        assertEquals("每周二、四、六", repeatRuleLabel(RepeatRule.Weekly, repeatWeekdays = setOf(2, 4, 6)))
    }

    @Test
    fun workdayAndWeekendPresetsHaveDedicatedLabels() {
        assertEquals("工作日", repeatRuleLabel(RepeatRule.Weekly, repeatWeekdays = setOf(1, 2, 3, 4, 5)))
        assertEquals("周末", repeatRuleLabel(RepeatRule.Weekly, repeatWeekdays = setOf(6, 7)))
    }

    @Test
    fun intervalWithWeekdaysCombinesBoth() {
        assertEquals(
            "每 2 周 · 周一、三",
            repeatRuleLabel(RepeatRule.Weekly, repeatInterval = 2, repeatWeekdays = setOf(1, 3))
        )
        assertEquals(
            "每 2 周 · 工作日",
            repeatRuleLabel(RepeatRule.Weekly, repeatInterval = 2, repeatWeekdays = setOf(1, 2, 3, 4, 5))
        )
    }

    @Test
    fun invalidValuesFallBackGracefully() {
        assertEquals("每天", repeatRuleLabel(RepeatRule.Daily, repeatInterval = 0))
        assertEquals("每周", repeatRuleLabel(RepeatRule.Weekly, repeatWeekdays = setOf(9)))
    }
}
