package `fun`.han1997.dianmao.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeFormatters {
    private val dateTime = SimpleDateFormat("M月d日 HH:mm", Locale.CHINA)
    private val dateOnly = SimpleDateFormat("M月d日", Locale.CHINA)

    fun labelFor(dateMillis: Long, minutesOfDay: Int?): String {
        return if (minutesOfDay == null) {
            dateOnly.format(Date(dateMillis))
        } else {
            val hour = minutesOfDay / 60
            val minute = minutesOfDay % 60
            "${dateOnly.format(Date(dateMillis))} ${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        }
    }

    fun alarmLabel(dateTimeMillis: Long): String = dateTime.format(Date(dateTimeMillis))
}

