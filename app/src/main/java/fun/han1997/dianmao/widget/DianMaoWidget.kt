package `fun`.han1997.dianmao.widget

import `fun`.han1997.dianmao.HomeSmartTaskItem
import `fun`.han1997.dianmao.HomeSmartView
import `fun`.han1997.dianmao.MainActivity
import `fun`.han1997.dianmao.R
import `fun`.han1997.dianmao.alerts.TaskReminderReceiver
import `fun`.han1997.dianmao.alerts.completeTaskSilently
import `fun`.han1997.dianmao.buildHomeSmartTaskItems
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalSize
import androidx.glance.ColorFilter
import androidx.glance.ImageProvider
import androidx.glance.Image
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.first
import java.time.ZoneId

/** Warm paper palette matching the app's light/dark themes. */
private val PaperDay = Color(0xFFF6F1E9)
private val PaperNight = Color(0xFF2A221C)
private val InkDay = Color(0xFF2B211B)
private val InkNight = Color(0xFFEDE6DA)
private val AccentDay = Color(0xFFA94332)
private val AccentNight = Color(0xFFD98A76)

internal const val WIDGET_TASK_LIMIT = 4
internal const val WIDGET_WIDE_TASK_LIMIT = 8
private val WideBreakpoint = 280.dp

class DianMaoHomeWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Responsive(
        setOf(
            DpSize(180.dp, 110.dp),
            DpSize(280.dp, 110.dp)
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = TaskRepository(context.applicationContext)
        val tasks = repository.activeTasks.first()
        val nowMillis = System.currentTimeMillis()

        provideContent {
            GlanceTheme {
                DianMaoWidgetContent(tasks, context, nowMillis)
            }
        }
    }
}

class DianMaoWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DianMaoHomeWidget()
}

/**
 * Forces a widget refresh after the app mutates task data or on app start.
 * The system's own 30-minute update period is the safety net.
 */
suspend fun refreshHomeWidget(context: Context) {
    runCatching { DianMaoHomeWidget().updateAll(context) }
}

private fun openAppIntent(context: Context): Intent {
    return Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
}

private fun quickAddIntent(context: Context): Intent {
    return Intent(context, MainActivity::class.java).apply {
        putExtra(MainActivity.EXTRA_OPEN_ADD, true)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
}

private fun openTaskIntent(context: Context, taskId: Long): Intent {
    return Intent(context, MainActivity::class.java).apply {
        putExtra(TaskReminderReceiver.EXTRA_TASK_ID, taskId)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
    }
}

@Composable
private fun DianMaoWidgetContent(tasks: List<TaskUiModel>, context: Context, nowMillis: Long) {
    val paper = ColorProvider(PaperDay, PaperNight)
    val ink = ColorProvider(InkDay, InkNight)
    val accent = ColorProvider(AccentDay, AccentNight)
    val taskLimit = if (LocalSize.current.width >= WideBreakpoint) {
        WIDGET_WIDE_TASK_LIMIT
    } else {
        WIDGET_TASK_LIMIT
    }
    val todayItems = buildHomeSmartTaskItems(
        tasks = tasks,
        view = HomeSmartView.Today,
        query = "",
        nowMillis = nowMillis,
        zoneId = ZoneId.systemDefault()
    ).take(taskLimit)

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(paper)
            .padding(12.dp)
            .clickable(actionStartActivity(openAppIntent(context))),
        verticalAlignment = Alignment.Top
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DianMao · 今天",
                style = TextStyle(color = ink, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Text(
                text = "${todayItems.size}",
                style = TextStyle(color = accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }

        if (todayItems.isEmpty()) {
            Text(
                text = "今天没有待办",
                style = TextStyle(color = ink, fontSize = 13.sp),
                modifier = GlanceModifier.padding(top = 8.dp)
            )
        } else {
            todayItems.forEach { item ->
                WidgetTaskRow(item, ink, accent, context)
            }
        }

        Spacer(modifier = GlanceModifier.defaultWeight())
        Row(
            modifier = GlanceModifier
                .fillMaxWidth()
                .clickable(actionStartActivity(quickAddIntent(context))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "＋ 记一笔",
                style = TextStyle(color = accent, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            )
        }
    }
}

@Composable
private fun WidgetTaskRow(
    item: HomeSmartTaskItem,
    ink: ColorProvider,
    accent: ColorProvider,
    context: Context
) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(R.drawable.ic_widget_check_circle),
            contentDescription = "完成「${item.task.title}」",
            modifier = GlanceModifier
                .size(20.dp)
                .clickable(
                    actionRunCallback<ToggleWidgetTaskCallback>(
                        parameters = actionParametersOf(
                            ToggleWidgetTaskCallback.TASK_ID_KEY to item.task.id
                        )
                    )
                ),
            colorFilter = ColorFilter.tint(accent)
        )
        Row(
            modifier = GlanceModifier
                .defaultWeight()
                .padding(start = 8.dp)
                .clickable(actionStartActivity(openTaskIntent(context, item.task.id))),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.task.title,
                style = TextStyle(color = ink, fontSize = 13.sp),
                maxLines = 1
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            item.dueLabel?.let { label ->
                Text(
                    text = label,
                    style = TextStyle(color = accent, fontSize = 11.sp),
                    maxLines = 1
                )
            }
        }
    }
}

class ToggleWidgetTaskCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val taskId = parameters[TASK_ID_KEY] ?: return
        if (taskId <= 0L) return
        val appContext = context.applicationContext
        val task = TaskRepository(appContext).findTaskById(taskId) ?: return
        completeTaskSilently(appContext, task)
        refreshHomeWidget(appContext)
    }

    companion object {
        val TASK_ID_KEY = ActionParameters.Key<Long>("task_id")
    }
}
