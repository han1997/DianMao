package `fun`.han1997.dianmao.alerts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import `fun`.han1997.dianmao.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderRestoreReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val appContext = context.applicationContext
                val repository = TaskRepository(appContext)
                val scheduler = ReminderScheduler(appContext)
                repository.restoreScheduledReminders().forEach { task ->
                    scheduler.schedule(task)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
