package `fun`.han1997.dianmao

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class QuickDueDateOptionsTest {

    private val zone: ZoneId = ZoneId.of("Asia/Shanghai")

    private fun millisOf(date: LocalDate): Long =
        date.atStartOfDay(zone).toInstant().toEpochMilli()

    @Test
    fun `options are today tomorrow and next monday`() {
        // Wednesday 2026-09-02 15:30 local
        val now = LocalDate.of(2026, 9, 2).atTime(15, 30).atZone(zone).toInstant().toEpochMilli()

        val options = quickDueDateOptions(now, zone)

        assertEquals(listOf("今天", "明天", "下周一"), options.map { it.label })
        assertEquals(millisOf(LocalDate.of(2026, 9, 2)), options[0].dateMillis)
        assertEquals(millisOf(LocalDate.of(2026, 9, 3)), options[1].dateMillis)
        assertEquals(millisOf(LocalDate.of(2026, 9, 7)), options[2].dateMillis)
    }

    @Test
    fun `next monday from sunday is tomorrow in iso weeks`() {
        // Sunday 2026-09-06 belongs to the ISO week 2026-08-31..09-06, so the
        // Monday of next week is 2026-09-07 (tomorrow).
        val now = LocalDate.of(2026, 9, 6).atTime(9, 0).atZone(zone).toInstant().toEpochMilli()

        val options = quickDueDateOptions(now, zone)

        assertEquals(millisOf(LocalDate.of(2026, 9, 7)), options[2].dateMillis)
    }

    @Test
    fun `next monday from monday is seven days out`() {
        // Monday 2026-08-31 local
        val now = LocalDate.of(2026, 8, 31).atTime(23, 59).atZone(zone).toInstant().toEpochMilli()

        val options = quickDueDateOptions(now, zone)

        assertEquals(millisOf(LocalDate.of(2026, 9, 7)), options[2].dateMillis)
    }

    @Test
    fun `option timestamps are local midnight regardless of the current time`() {
        val now = LocalDate.of(2026, 9, 2).atTime(23, 59).atZone(zone).toInstant().toEpochMilli()

        val options = quickDueDateOptions(now, zone)

        options.forEach { option ->
            val date = Instant.ofEpochMilli(option.dateMillis).atZone(zone).toLocalDate()
            assertEquals(
                option.dateMillis,
                date.atStartOfDay(zone).toInstant().toEpochMilli()
            )
        }
    }
}
