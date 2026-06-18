package `fun`.han1997.dianmao

import android.provider.CalendarContract
import `fun`.han1997.dianmao.calendar.CalendarCandidate
import `fun`.han1997.dianmao.calendar.chooseBestCalendarId
import `fun`.han1997.dianmao.calendar.reminderIntentExtras
import `fun`.han1997.dianmao.calendar.reminderConfigsToTry
import org.junit.Assert.assertEquals
import org.junit.Test

class CalendarWriterSelectionTest {
    @Test
    fun prefersCalendarThatSupportsAlertReminders() {
        val calendarId = chooseBestCalendarId(
            listOf(
                CalendarCandidate(
                    id = 11L,
                    isPrimary = true,
                    accessLevel = CalendarContract.Calendars.CAL_ACCESS_OWNER,
                    allowedReminderMethods = emptySet()
                ),
                CalendarCandidate(
                    id = 22L,
                    isPrimary = false,
                    accessLevel = CalendarContract.Calendars.CAL_ACCESS_OWNER,
                    allowedReminderMethods = setOf(CalendarContract.Reminders.METHOD_ALERT)
                )
            )
        )

        assertEquals(22L, calendarId)
    }

    @Test
    fun fallsBackToPrimaryWritableCalendarWhenReminderSupportIsUnknown() {
        val calendarId = chooseBestCalendarId(
            listOf(
                CalendarCandidate(
                    id = 11L,
                    isPrimary = true,
                    accessLevel = CalendarContract.Calendars.CAL_ACCESS_EDITOR,
                    allowedReminderMethods = null
                ),
                CalendarCandidate(
                    id = 22L,
                    isPrimary = false,
                    accessLevel = CalendarContract.Calendars.CAL_ACCESS_EDITOR,
                    allowedReminderMethods = null
                )
            )
        )

        assertEquals(11L, calendarId)
    }

    @Test
    fun ignoresCalendarsWithoutContributorAccess() {
        val calendarId = chooseBestCalendarId(
            listOf(
                CalendarCandidate(
                    id = 11L,
                    isPrimary = true,
                    accessLevel = CalendarContract.Calendars.CAL_ACCESS_READ,
                    allowedReminderMethods = setOf(CalendarContract.Reminders.METHOD_ALERT)
                )
            )
        )

        assertEquals(11L, calendarId)
    }

    @Test
    fun stillReturnsCalendarWhenAllCandidatesLookNonWritable() {
        val calendarId = chooseBestCalendarId(
            listOf(
                CalendarCandidate(
                    id = 11L,
                    isPrimary = true,
                    accessLevel = 0,
                    allowedReminderMethods = null
                ),
                CalendarCandidate(
                    id = 22L,
                    isPrimary = false,
                    accessLevel = 0,
                    allowedReminderMethods = setOf(CalendarContract.Reminders.METHOD_ALERT)
                )
            )
        )

        assertEquals(22L, calendarId)
    }

    @Test
    fun prefersAllowedReminderMethodsWhenProviderDeclaresThem() {
        val methods = reminderConfigsToTry(
            setOf(
                CalendarContract.Reminders.METHOD_DEFAULT,
                CalendarContract.Reminders.METHOD_ALARM
            )
        ).map { it.method }

        assertEquals(
            listOf(
                CalendarContract.Reminders.METHOD_DEFAULT,
                CalendarContract.Reminders.METHOD_ALARM,
                CalendarContract.Reminders.METHOD_ALERT
            ),
            methods
        )
    }

    @Test
    fun insertIntentExtrasRequestImmediateAlertReminder() {
        assertEquals(
            mapOf(
                CalendarContract.Events.HAS_ALARM to 1,
                CalendarContract.Reminders.MINUTES to 0,
                CalendarContract.Reminders.METHOD to CalendarContract.Reminders.METHOD_ALERT
            ),
            reminderIntentExtras()
        )
    }
}
