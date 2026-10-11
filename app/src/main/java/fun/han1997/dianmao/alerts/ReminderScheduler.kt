package `fun`.han1997.dianmao.alerts

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskUiModel

class ReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)

    /**
     * Schedules the local reminders for [task]: one exact alarm per selected
     * lead time (due moment pulled back by the lead). Tasks without a due
     * date or with the local reminder off are ignored.
     */
    fun schedule(task: TaskUiModel) {
        val dueAt = task.dueAtMillis ?: return
        if (!task.useLocalReminder) return
        task.reminderLeadMinutes.forEach { lead ->
            val triggerAt = dueAt - lead * 60_000L
            if (triggerAt <= System.currentTimeMillis()) return@forEach
            val pendingIntent = reminderPendingIntent(
                context = context,
                taskId = task.id,
                title = task.title,
                leadMinutes = lead,
                isRecurring = task.repeatRule != RepeatRule.None,
                flags = PendingIntent.FLAG_UPDATE_CURRENT
            )
            if (pendingIntent != null) {
                setAlarm(triggerAt, pendingIntent)
            }
        }
    }

    /**
     * Cancels every alarm scheduled for [taskId] across [leadMinutes]. The
     * caller passes the task's current effective lead set — the same set the
     * schedule path used.
     */
    fun cancel(taskId: Long, leadMinutes: Set<Int>) {
        leadMinutes.forEach { lead ->
            val pendingIntent = reminderPendingIntent(
                context = context,
                taskId = taskId,
                title = "",
                leadMinutes = lead,
                isRecurring = false,
                flags = PendingIntent.FLAG_NO_CREATE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    /**
     * Reschedules a single reminder for [taskId] at [ReminderSnooze] delay
     * from now, reusing the same (taskId, lead) request code so a later
     * [cancel] also clears the snoozed alarm.
     */
    fun snooze(taskId: Long, title: String, leadMinutes: Int, isRecurring: Boolean) {
        val triggerAt = ReminderSnooze.triggerAtMillis(System.currentTimeMillis())
        val pendingIntent = reminderPendingIntent(
            context = context,
            taskId = taskId,
            title = title,
            leadMinutes = leadMinutes,
            isRecurring = isRecurring,
            flags = PendingIntent.FLAG_UPDATE_CURRENT
        )
        if (pendingIntent != null) {
            setAlarm(triggerAt, pendingIntent)
        }
    }

    /**
     * True when exact alarms are permitted (pre-S always, otherwise the
     * system 「闹钟和提醒」toggle). The UI surfaces a guidance notice when
     * this is false but reminders are armed — [setAlarm] silently degrades
     * to inexact alarms that can fire late.
     */
    fun canScheduleExactAlarms(): Boolean {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
    }

    private fun setAlarm(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        if (canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }
}

internal object ReminderSnooze {
    const val DELAY_MILLIS: Long = 10 * 60_000L

    fun triggerAtMillis(nowMillis: Long, delayMillis: Long = DELAY_MILLIS): Long = nowMillis + delayMillis
}

/**
 * Builds the reminder broadcast intent/PendingIntent shared by the schedule,
 * cancel, and snooze paths. Extras do not participate in PendingIntent
 * matching, so FLAG_NO_CREATE callers may pass a bare intent shape.
 */
internal fun reminderPendingIntent(
    context: Context,
    taskId: Long,
    title: String,
    leadMinutes: Int,
    isRecurring: Boolean,
    flags: Int
): PendingIntent? {
    val intent = Intent(context, TaskReminderReceiver::class.java).apply {
        putExtra(TaskReminderReceiver.EXTRA_TITLE, title)
        putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
        putExtra(TaskReminderReceiver.EXTRA_LEAD_MINUTES, leadMinutes)
        putExtra(TaskReminderReceiver.EXTRA_IS_RECURRING, isRecurring)
    }
    return PendingIntent.getBroadcast(
        context,
        reminderRequestCode(taskId, leadMinutes),
        intent,
        flags or PendingIntentCompatFlags.immutable()
    )
}

/**
 * Stable request code distinct per (taskId, lead time). Long/Int hashCode and
 * Arrays.hashCode are value-based, so the code is deterministic across
 * process restarts — required for FLAG_UPDATE_CURRENT/NO_CREATE matching.
 */
internal fun reminderRequestCode(taskId: Long, leadMinutes: Int): Int {
    var result = taskId.hashCode()
    result = 31 * result + leadMinutes
    return result
}

internal enum class ReminderActionKind {
    Complete,
    Snooze
}

/**
 * Request code for notification action PendingIntents. Deliberately mixed
 * with a salt so action intents never collide with the alarm request codes
 * derived from [reminderRequestCode].
 */
internal fun reminderActionRequestCode(taskId: Long, leadMinutes: Int, action: ReminderActionKind): Int {
    var result = reminderRequestCode(taskId, leadMinutes)
    result = 31 * result + action.ordinal
    result = 31 * result + ACTION_REQUEST_CODE_SALT
    return result
}

private const val ACTION_REQUEST_CODE_SALT = 0x51ED

internal object PendingIntentCompatFlags {
    fun immutable(): Int = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
}
