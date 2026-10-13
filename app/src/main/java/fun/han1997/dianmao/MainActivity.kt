package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons

import `fun`.han1997.dianmao.alerts.ReminderScheduler
import `fun`.han1997.dianmao.alerts.TaskNotificationChannels
import `fun`.han1997.dianmao.alerts.TaskReminderReceiver
import `fun`.han1997.dianmao.alerts.completeTaskSilently
import `fun`.han1997.dianmao.backup.BackupSettingsStore
import `fun`.han1997.dianmao.backup.WebDavBackupClient
import `fun`.han1997.dianmao.backup.WEBDAV_PRE_RESTORE_SNAPSHOT_FILE_NAME
import `fun`.han1997.dianmao.backup.shouldAutoBackupNow
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.toUiModel
import `fun`.han1997.dianmao.widget.refreshHomeWidget
import java.time.ZoneId
import `fun`.han1997.dianmao.TaskRow
import `fun`.han1997.dianmao.rememberDragDropListState
import `fun`.han1997.dianmao.HomeSmartView
import `fun`.han1997.dianmao.InAppBackTarget
import `fun`.han1997.dianmao.buildHomeSmartTaskItems
import `fun`.han1997.dianmao.resolveInAppBackTarget
import `fun`.han1997.dianmao.TaskSaveOutcome
import `fun`.han1997.dianmao.defaultBackupFileName
import `fun`.han1997.dianmao.deleteCalendarEvent
import `fun`.han1997.dianmao.openAppNotificationSettings
import `fun`.han1997.dianmao.openCalendarReminder
import `fun`.han1997.dianmao.readTextFromUri
import `fun`.han1997.dianmao.saveOrOpenCalendarEvent
import `fun`.han1997.dianmao.writeTextToUri
import `fun`.han1997.dianmao.AddTaskPanel
import `fun`.han1997.dianmao.ArchivedPanel
import `fun`.han1997.dianmao.BackupPanel
import `fun`.han1997.dianmao.CompletionNoteDialog
import `fun`.han1997.dianmao.DeleteTaskDialog
import `fun`.han1997.dianmao.UncompleteRecurringTaskDialog
import `fun`.han1997.dianmao.AppHeader
import `fun`.han1997.dianmao.QuadrantBoard
import `fun`.han1997.dianmao.TimeFocusLane
import `fun`.han1997.dianmao.QuadrantDetailCompletedGroup
import `fun`.han1997.dianmao.QuadrantDetailEmptyState
import `fun`.han1997.dianmao.QuadrantDetailHeader
import `fun`.han1997.dianmao.QuadrantDetailIntro
import `fun`.han1997.dianmao.QuadrantDetailSectionLabel
import `fun`.han1997.dianmao.TaskDetailScreen
import `fun`.han1997.dianmao.SettingsScreen
import `fun`.han1997.dianmao.AppearanceScreen
import `fun`.han1997.dianmao.appearance.AppearanceSettingsStore
import `fun`.han1997.dianmao.appearance.ThemeMode
import androidx.compose.foundation.isSystemInDarkTheme
import `fun`.han1997.dianmao.DianMaoTheme
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import java.io.IOException
import java.time.Instant
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        TaskNotificationChannels.ensure(this)

        setContent {
            // Prototype-first: appearance is a persisted user preference
            // (system / light / dark), read here so the whole tree — including
            // dialogs and the widget preview — follows the chosen mode.
            val appearanceStore = remember { AppearanceSettingsStore(applicationContext) }
            var themeMode by remember { mutableStateOf(appearanceStore.loadThemeMode()) }
            DianMaoTheme(themeMode = themeMode) {
                DianMaoApp(
                    initialTaskId = extractReminderTaskId(intent),
                    initialOpenAdd = extractOpenAdd(intent),
                    themeMode = themeMode,
                    onThemeModeChange = { mode ->
                        themeMode = mode
                        appearanceStore.saveThemeMode(mode)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val taskId = extractReminderTaskId(intent)
        if (taskId != null) {
            intent.removeExtra(TaskReminderReceiver.EXTRA_TASK_ID)
            currentComposeTaskId?.invoke(taskId)
        }
        if (extractOpenAdd(intent)) {
            intent.removeExtra(EXTRA_OPEN_ADD)
            currentComposeOpenAdd?.invoke()
        }
    }

    private fun extractReminderTaskId(intent: Intent?): Long? {
        if (intent == null || !intent.hasExtra(TaskReminderReceiver.EXTRA_TASK_ID)) return null
        val id = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, 0L)
        return if (id > 0L) id else null
    }

    private fun extractOpenAdd(intent: Intent?): Boolean {
        return intent?.getBooleanExtra(EXTRA_OPEN_ADD, false) == true
    }

    companion object {
        // Set from composition so onNewIntent can forward deep-link task ids
        // and quick-add requests into the Compose tree without a shared
        // ViewModel.
        @Volatile
        var currentComposeTaskId: ((Long) -> Unit)? = null

        @Volatile
        var currentComposeOpenAdd: (() -> Unit)? = null

        const val EXTRA_OPEN_ADD = "extra_open_add"
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DianMaoApp(
    initialTaskId: Long? = null,
    initialOpenAdd: Boolean = false,
    themeMode: ThemeMode = ThemeMode.System,
    onThemeModeChange: (ThemeMode) -> Unit = {}
) {
    val context = LocalContext.current
    val repository = remember(context) { TaskRepository(context.applicationContext) }
    val scheduler = remember(context) { ReminderScheduler(context.applicationContext) }
    val backupSettingsStore = remember(context) { BackupSettingsStore(context.applicationContext) }
    val webDavClient = remember { WebDavBackupClient() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeTasks by repository.activeTasks.collectAsState(initial = emptyList())
    val archivedTasks by repository.archivedTasks.collectAsState(initial = emptyList())
    val completionsWithTask by repository.completionsWithTask.collectAsState(initial = emptyList())
    val zoneId = remember { ZoneId.systemDefault() }
    // Recomputed at each local midnight so the 今天/本周 buckets in review
    // stats and the calendar view never go stale while the app stays open
    // across midnight. A plain remember would freeze the timestamp at first
    // composition; day-boundary refresh is enough because all consumers
    // bucket by day, never by hour.
    val nowMillis by produceState(System.currentTimeMillis()) {
        while (true) {
            val zone = ZoneId.systemDefault()
            val nextMidnight = Instant.ofEpochMilli(value)
                .atZone(zone).toLocalDate().plusDays(1)
                .atStartOfDay(zone).toInstant().toEpochMilli()
            delay((nextMidnight - System.currentTimeMillis()).coerceAtLeast(0L))
            value = System.currentTimeMillis()
        }
    }
    val reviewEvents = remember(completionsWithTask, activeTasks, archivedTasks) {
        buildReviewCompletionEvents(completionsWithTask, activeTasks + archivedTasks)
    }
    val reviewStats = remember(reviewEvents, nowMillis) { buildReviewStats(reviewEvents, nowMillis, zoneId) }

    var activePriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showSettingsPanel by rememberSaveable { mutableStateOf(false) }
    var showAppearancePanel by rememberSaveable { mutableStateOf(false) }
    var showWidgetPanel by rememberSaveable { mutableStateOf(false) }
    var showAboutDialog by rememberSaveable { mutableStateOf(false) }
    // Prototype-first: backup/archive/review/widget are entered from the
    // settings hub (state.from), so their back/close returns to settings
    // rather than jumping straight home.
    var returnToSettings by rememberSaveable { mutableStateOf(false) }
    var showBackupPanel by rememberSaveable { mutableStateOf(false) }
    var backupSyncInProgress by remember { mutableStateOf(false) }
    var showArchivedPanel by rememberSaveable { mutableStateOf(false) }
    var showReviewPanel by rememberSaveable { mutableStateOf(false) }
    var showCalendarView by rememberSaveable { mutableStateOf(false) }
    var calendarPresetDueAtMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var showSelectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedTaskIds by remember { mutableStateOf(setOf<Long>()) }
    var batchMoveQuadrantTarget by remember { mutableStateOf(false) }
    var batchDeleteConfirm by remember { mutableStateOf(false) }
    var taskForNote by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForEdit by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForDelete by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForUncompletion by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForDetail by remember { mutableStateOf<TaskUiModel?>(null) }
    var homeSmartView by rememberSaveable { mutableStateOf(HomeSmartView.Quadrants) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    var homeSearchVisible by rememberSaveable { mutableStateOf(false) }
    val homeSmartItems = remember(activeTasks, homeSmartView, homeSearchQuery, nowMillis) {
        buildHomeSmartTaskItems(
            tasks = activeTasks,
            view = homeSmartView,
            query = homeSearchQuery,
            nowMillis = nowMillis
        )
    }
    val homeVisibleTasks = remember(homeSmartItems) { homeSmartItems.map { it.task } }

    // Refresh taskForDetail when the task data changes (after edit, complete, etc.)
    LaunchedEffect(activeTasks, archivedTasks, taskForDetail?.id) {
        taskForDetail?.let { current ->
            val updated = (activeTasks + archivedTasks).firstOrNull { it.id == current.id }
            if (updated != null && updated != current) {
                taskForDetail = updated
            }
        }
    }

    // Notification deep link: open the reminder's task detail once the task
    // lists are loaded. Cold start passes the id through initialTaskId; warm
    // start forwards it from onNewIntent via the activity callback. A missing
    // task (deleted) is silently ignored.
    var pendingDeepLinkTaskId by remember { mutableStateOf(initialTaskId) }
    LaunchedEffect(activeTasks, archivedTasks) {
        val linkId = pendingDeepLinkTaskId ?: return@LaunchedEffect
        if (taskForDetail?.id == linkId) {
            pendingDeepLinkTaskId = null
            return@LaunchedEffect
        }
        val linked = (activeTasks + archivedTasks).firstOrNull { it.id == linkId }
        if (linked != null) {
            taskForDetail = linked
            pendingDeepLinkTaskId = null
        }
    }
    LaunchedEffect(Unit) {
        MainActivity.currentComposeTaskId = { linkId -> pendingDeepLinkTaskId = linkId }
    }
    DisposableEffect(Unit) {
        onDispose {
            MainActivity.currentComposeTaskId = null
            MainActivity.currentComposeOpenAdd = null
        }
    }

    // Quick-add deep link (home-screen widget): open the add sheet once the
    // base surface is up. Cold start passes the flag through initialOpenAdd;
    // warm start forwards it from onNewIntent via the activity callback.
    var pendingOpenAdd by remember { mutableStateOf(initialOpenAdd) }
    LaunchedEffect(pendingOpenAdd) {
        if (pendingOpenAdd) {
            showAddSheet = true
            pendingOpenAdd = false
        }
    }
    LaunchedEffect(Unit) {
        MainActivity.currentComposeOpenAdd = { pendingOpenAdd = true }
    }

    // Refresh the home-screen widget whenever the active task data changes,
    // so its today list always matches the current data (add/complete/delete
    // all land here). The system's own 30-minute update period is the
    // safety net for changes outside the app.
    LaunchedEffect(activeTasks) {
        refreshHomeWidget(context)
    }
    var webDavSettings by remember { mutableStateOf(backupSettingsStore.loadWebDavSettings()) }
    var autoBackupSettings by remember { mutableStateOf(backupSettingsStore.loadAutoBackupSettings()) }

    // Auto backup: on app start, upload the latest data to WebDAV when the
    // daily interval has elapsed (or the last attempt failed). Silent — a
    // failed attempt retries on the next start.
    LaunchedEffect(Unit) {
        val webDav = backupSettingsStore.loadWebDavSettings()
        if (!shouldAutoBackupNow(
                enabled = autoBackupSettings.enabled,
                webDavConfigured = webDav.isConfigured,
                lastAutoBackupAtMillis = autoBackupSettings.lastAutoBackupAtMillis,
                lastAutoBackupSucceeded = autoBackupSettings.lastAutoBackupSucceeded
            )
        ) {
            return@LaunchedEffect
        }
        try {
            webDavClient.upload(webDav, repository.exportBackupJson())
            backupSettingsStore.recordAutoBackupResult(System.currentTimeMillis(), true)
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            backupSettingsStore.recordAutoBackupResult(System.currentTimeMillis(), false)
        }
        autoBackupSettings = backupSettingsStore.loadAutoBackupSettings()
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }
    val localBackupExportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    writeTextToUri(context, uri, repository.exportBackupJson())
                }.onSuccess {
                    snackbarHostState.showSnackbar("本地备份已导出")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("导出失败：${error.message.orEmpty()}")
                }
            }
        }
    }
    val localBackupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                runCatching {
                    val count = repository.importBackupJson(readTextFromUri(context, uri))
                    repository.restoreScheduledReminders().forEach { task ->
                        scheduler.schedule(task)
                    }
                    count
                }.onSuccess { count ->
                    snackbarHostState.showSnackbar("已从本地备份恢复 $count 个事项")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("恢复失败：${error.message.orEmpty()}")
                }
            }
        }
    }

    LaunchedEffect(repository, scheduler) {
        repository.resetExpiredCycleTasks()
        // Re-arm local reminders after process start (covers reboot-less kills;
        // reboot itself is handled by ReminderRestoreReceiver).
        repository.restoreScheduledReminders().forEach { task ->
            scheduler.schedule(task)
        }
        // Android 14+ ships with the exact-alarm permission off by default,
        // and the scheduler silently downgrades to inexact alarms that can
        // fire late. When reminders are armed but the permission is missing,
        // surface a once-per-open guidance notice pointing at the toggle.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !scheduler.canScheduleExactAlarms() &&
            repository.hasActiveLocalReminderTasks()
        ) {
            snackbarHostState.showSnackbar(
                message = "到点提醒可能延迟：系统未授予「闹钟和提醒」权限，请到系统设置开启",
                duration = SnackbarDuration.Long
            )
        }
    }

    val quadrantDetailTasks = remember(activeTasks, activePriority) {
        activePriority?.let { priority -> activeTasks.filter { it.quadrant == priority } }.orEmpty()
    }

    var reorderableDetailTasks by remember { mutableStateOf<List<TaskUiModel>>(emptyList()) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(quadrantDetailTasks) {
        if (!isDragging) {
            reorderableDetailTasks = quadrantDetailTasks
        }
    }

    val toggleComplete: (TaskUiModel) -> Unit = { target ->
        if (target.completed) {
            if (target.repeatRule != RepeatRule.None) {
                taskForUncompletion = target
            } else {
                scope.launch {
                    val succeeded = snackbarHostState.runOrNotifyFailure("标记失败") {
                        repository.toggleCompleted(target, false)
                        if (target.useLocalReminder && target.dueAtMillis != null) {
                            scheduler.schedule(target)
                        }
                    }
                    if (succeeded) {
                        snackbarHostState.showSnackbar("事项已标为未完成")
                    }
                }
            }
        } else {
            taskForNote = target
        }
    }

    // Silent completion for the swipe gesture: no note dialog interrupting the
    // flow. The top notice offers an optional 写心得 action that opens the
    // completion-note dialog afterwards.
    val completeSilently: (TaskUiModel) -> Unit = { target ->
        scope.launch {
            snackbarHostState.runOrNotifyFailure("完成失败") {
                val completed = completeTaskSilently(context, target)
                if (!completed) return@runOrNotifyFailure
                val result = snackbarHostState.showSnackbar(
                    message = "已完成「${target.title}」",
                    actionLabel = "写心得",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    taskForNote = target
                }
            }
        }
    }

    val archiveTask: (TaskUiModel) -> Unit = { target ->
        scope.launch {
            val succeeded = snackbarHostState.runOrNotifyFailure("归档失败") {
                repository.archiveTask(target.id)
            }
            if (succeeded) {
                snackbarHostState.showSnackbar("事项已归档")
            }
        }
    }

    // Multi-select (batch operations) state helpers.
    val enterSelection: (TaskUiModel) -> Unit = { target ->
        showSelectionMode = true
        selectedTaskIds = setOf(target.id)
    }
    val toggleSelection: (TaskUiModel) -> Unit = { target ->
        selectedTaskIds = if (target.id in selectedTaskIds) {
            selectedTaskIds - target.id
        } else {
            selectedTaskIds + target.id
        }
    }
    val exitSelection = {
        showSelectionMode = false
        selectedTaskIds = emptySet()
    }
    val selectedTasks = remember(selectedTaskIds, activeTasks) {
        activeTasks.filter { it.id in selectedTaskIds }
    }

    LaunchedEffect(activeTasks, showSelectionMode) {
        if (showSelectionMode) {
            val activeIds = activeTasks.map { it.id }.toSet()
            val remaining = reconcileSelectedTaskIds(selectedTaskIds, activeIds)
            if (remaining != selectedTaskIds) {
                selectedTaskIds = remaining
            }
            if (remaining.isEmpty()) {
                showSelectionMode = false
            }
        }
    }

    val batchMoveQuadrant: (TaskPriority) -> Unit = batchMoveQuadrant@{ priority ->
        val targets = selectedTasks
        if (targets.isEmpty()) return@batchMoveQuadrant
        scope.launch {
            val succeeded = snackbarHostState.runOrNotifyFailure("批量移动失败") {
                repository.moveTasksToQuadrant(targets.map { it.id }, priority.type)
            }
            if (succeeded) {
                exitSelection()
                snackbarHostState.showSnackbar("已移动 ${targets.size} 个事项到「${priority.shortTitle}」")
            }
        }
    }

    val batchDelete: () -> Unit = batchDelete@{
        val targets = selectedTasks
        if (targets.isEmpty()) return@batchDelete
        scope.launch {
            snackbarHostState.runOrNotifyFailure("批量删除失败") {
                targets.forEach { scheduler.cancel(it.id, it.reminderLeadMinutes) }
                val removed = targets.mapNotNull { repository.deleteTask(it.id) }
                exitSelection()
                val result = snackbarHostState.showSnackbar(
                    message = "已删除 ${removed.size} 个事项",
                    actionLabel = "撤销",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    removed.forEach { entity ->
                        repository.restoreTask(entity)
                        scheduler.schedule(entity.toUiModel())
                    }
                } else {
                    removed.forEach { entity -> deleteCalendarEvent(context, entity.calendarEventId) }
                }
            }
        }
    }

    // Shared completion-record callbacks: the task detail surface and the
    // recurring-task history panel expose the exact same 删除完成记录（带撤销）/
    // 更新完成心得 behaviour, so the lambdas live here once instead of being
    // duplicated at both call sites.
    val deleteCompletionWithUndo: (Long) -> Unit = { completionId ->
        scope.launch {
            snackbarHostState.runOrNotifyFailure("删除完成记录失败") {
                val removed = repository.deleteCompletionForUndo(completionId)
                val result = snackbarHostState.showSnackbar(
                    message = "完成记录已删除",
                    actionLabel = "撤销",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    removed?.let { repository.restoreCompletion(it) }
                }
            }
        }
    }
    val updateCompletionNoteQuietly: (Long, String?) -> Unit = { completionId, note ->
        scope.launch {
            val succeeded = snackbarHostState.runOrNotifyFailure("保存完成心得失败") {
                repository.updateCompletionNote(completionId, note)
            }
            if (succeeded) {
                snackbarHostState.showSnackbar("完成心得已保存")
            }
        }
    }

    // Move an open non-recurring task to a new due date (calendar
    // long-press). Mirrors the edit-save side-effect chain: alarms are
    // cancelled and re-scheduled, the system calendar event is synced, and
    // the widget refreshes — partial failures surface as suffix notices.
    val moveTaskDueToDate: (TaskUiModel, Long) -> Unit = { target, targetDateMillis ->
        scope.launch {
            try {
                scheduler.cancel(target.id, target.reminderLeadMinutes)
                val moved = repository.moveTaskDueToDate(target.id, targetDateMillis)
                checkNotNull(moved) { "该事项不支持移动（重复事项请在编辑中调整）" }
                val sideEffectFailures = mutableListOf<String>()
                if (moved.useCalendar) {
                    try {
                        val eventId = saveOrOpenCalendarEvent(context, moved)
                        if (eventId != null && eventId != moved.calendarEventId) {
                            repository.updateCalendarEventId(moved.id, eventId)
                        }
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        sideEffectFailures += "系统日历未同步"
                    }
                }
                if (moved.useLocalReminder && moved.dueAtMillis != null) {
                    try {
                        scheduler.schedule(moved)
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        sideEffectFailures += "提醒未重排"
                    }
                }
                refreshHomeWidget(context)
                val base = "已移动到「${moved.dueLabel}」"
                snackbarHostState.showSnackbar(
                    if (sideEffectFailures.isEmpty()) base else "$base；${sideEffectFailures.distinct().joinToString("、")}"
                )
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                snackbarHostState.showSnackbar("移动失败：${error.message ?: "请稍后重试"}")
            }
        }
    }

    val isTaskPanelVisible = showAddSheet || taskForEdit != null
    val dismissTaskPanel = {
        showAddSheet = false
        taskForEdit = null
        // The calendar quick-add preset only applies to the sheet it opened;
        // clear it so the next manual add starts without a date.
        calendarPresetDueAtMillis = null
    }
    val backTarget = resolveInAppBackTarget(
        showBackupPanel = showBackupPanel,
        showArchivedPanel = showArchivedPanel,
        showReviewPanel = showReviewPanel,
        isTaskPanelVisible = isTaskPanelVisible,
        hasActivePriority = activePriority != null,
        taskForDetail = taskForDetail != null,
        showCalendarView = showCalendarView,
        showSelectionMode = showSelectionMode,
        showAppearancePanel = showAppearancePanel,
        showSettingsPanel = showSettingsPanel,
        showWidgetPanel = showWidgetPanel
    )

    BackHandler(enabled = backTarget != null) {
        when (backTarget) {
            // Prototype-first: the two-level settings hub collapses one level at
            // a time — appearance returns to settings, settings to home.
            InAppBackTarget.AppearancePanel -> {
                showAppearancePanel = false
                showSettingsPanel = true
            }
            InAppBackTarget.WidgetPanel -> {
                showWidgetPanel = false
                showSettingsPanel = true
            }
            InAppBackTarget.SettingsPanel -> showSettingsPanel = false
            InAppBackTarget.BackupPanel -> {
                showBackupPanel = false
                showSettingsPanel = returnToSettings
                returnToSettings = false
            }
            InAppBackTarget.ArchivedPanel -> {
                showArchivedPanel = false
                showSettingsPanel = returnToSettings
                returnToSettings = false
            }
            InAppBackTarget.ReviewPanel -> {
                showReviewPanel = false
                showSettingsPanel = returnToSettings
                returnToSettings = false
            }
            InAppBackTarget.TaskPanel -> dismissTaskPanel()
            InAppBackTarget.TaskDetail -> taskForDetail = null
            InAppBackTarget.CalendarView -> showCalendarView = false
            InAppBackTarget.TaskSelection -> {
                showSelectionMode = false
                selectedTaskIds = emptySet()
            }
            InAppBackTarget.QuadrantDetail -> activePriority = null
            null -> Unit
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                floatingActionButton = {
                    // Home and quadrant-detail surfaces, and never while an
                    // overlay panel or full-screen destination is open — the
                    // FAB lives above the content layer and would otherwise
                    // float over it. Prototype: #fab is hidden unless the page
                    // is home or quadrant.
                    if (taskForDetail == null && !showCalendarView &&
                        !showBackupPanel && !showArchivedPanel && !showReviewPanel &&
                        !showSettingsPanel && !showAppearancePanel && !showWidgetPanel &&
                        !isTaskPanelVisible
                    ) {
                        FloatingActionButton(
                            onClick = { showAddSheet = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            // Prototype .fab: 56x56 with a 16dp radius and a
                            // primary-tinted 6/16 drop shadow.
                            shape = RoundedCornerShape(16.dp),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 8.dp
                            )
                        ) {
                            Icon(
                                DianMaoIcons.Add,
                                contentDescription = "新增事项",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Home ↔ quadrant detail: calm 150ms crossfade instead of a
                // hard swap; snaps instantly when animations are disabled.
                Crossfade(
                    targetState = (activePriority != null),
                    animationSpec = motionTween(150),
                    label = "homeOrQuadrantDetail"
                ) { isDetail ->
                if (!isDetail) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                            .padding(start = 20.dp, top = 4.dp, end = 20.dp, bottom = 18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        if (showSelectionMode) {
                            // Prototype: while selecting, the whole header is
                            // replaced by close + "已选 N 项" + 全选.
                            SelectionHeader(
                                selectedCount = selectedTaskIds.size,
                                allSelected = selectedTaskIds.size == activeTasks.size && activeTasks.isNotEmpty(),
                                onExit = {
                                    showSelectionMode = false
                                    selectedTaskIds = emptySet()
                                },
                                onToggleAll = {
                                    selectedTaskIds = if (selectedTaskIds.size == activeTasks.size) {
                                        emptySet()
                                    } else {
                                        activeTasks.map { it.id }.toSet()
                                    }
                                }
                            )
                        } else {
                        AppHeader(
                            tasks = activeTasks,
                            searchVisible = homeSearchVisible,
                            query = homeSearchQuery,
                            onSearchToggle = {
                                if (homeSearchVisible) {
                                    homeSearchQuery = ""
                                }
                                homeSearchVisible = !homeSearchVisible
                            },
                            onQueryChanged = { homeSearchQuery = it },
                            onOpenSettings = {
                                showSettingsPanel = true
                            },
                            onOpenArchived = {
                                showArchivedPanel = true
                            },
                            onOpenReview = {
                                showReviewPanel = true
                            },
                            onOpenCalendar = {
                                showCalendarView = true
                            }
                        )
                        HomeSmartViewSwitcher(
                            selected = homeSmartView,
                            onSelect = { homeSmartView = it }
                        )
                        }
                        Crossfade(
                            targetState = homeSmartView,
                            animationSpec = motionTween(150),
                            label = "homeSmartView",
                            modifier = Modifier.weight(1f).fillMaxWidth()
                        ) { currentView ->
                            if (currentView == HomeSmartView.Quadrants) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    TimeFocusLane(
                                        tasks = homeVisibleTasks,
                                        onTaskSelected = { taskForDetail = it },
                                        nowMillis = nowMillis
                                    )
                                    QuadrantBoard(
                                        tasks = homeVisibleTasks,
                                        modifier = Modifier.weight(1f),
                                        onSelected = { priority -> activePriority = priority },
                                        onAddTask = { showAddSheet = true },
                                        // Prototype quadrantsView(): an empty
                                        // library with no search term replaces
                                        // the four panes with first-run guidance.
                                        firstRun = activeTasks.isEmpty(),
                                        emptyMessage = if (homeSearchQuery.isNotBlank() && homeVisibleTasks.isEmpty()) {
                                            "没有匹配“${homeSearchQuery.trim()}”的事项"
                                        } else {
                                            null
                                        }
                                    )
                                }
                            } else {
                                HomeSmartTaskList(
                                    items = homeSmartItems,
                                    homeSmartView = homeSmartView,
                                    repository = repository,
                                    nowMillis = nowMillis,
                                    onToggleComplete = toggleComplete,
                                    onCompleteSilently = completeSilently,
                                    onEditTask = { taskForEdit = it },
                                    onDeleteTask = { taskForDelete = it },
                                    onEditNote = { taskForNote = it },
                                    onArchiveTask = archiveTask,
                                    onViewDetail = { taskForDetail = it },
                                    onAddTask = { showAddSheet = true },
                                    isSelectionMode = showSelectionMode,
                                    selectedTaskIds = selectedTaskIds,
                                    onToggleSelection = { toggleSelection(it) },
                                    onLongPressTask = { enterSelection(it) },
                                    // Prototype smartList(): with no open task
                                    // left and no search term the list shows the
                                    // first-run guidance instead of the per-view
                                    // empty copy.
                                    firstRun = activeTasks.none { it.isOpenTask() },
                                    emptyMessage = if (homeSearchQuery.isNotBlank() && homeVisibleTasks.isEmpty()) {
                                        "没有匹配“${homeSearchQuery.trim()}”的事项"
                                    } else {
                                        null
                                    }
                                )
                            }
                        }
                    }
                } else {
                    val listState = rememberLazyListState()
                    val dragHaptics = LocalHapticFeedback.current
                    val openDetailTaskCount by remember {
                        derivedStateOf { reorderableDetailTasks.count { !it.completed } }
                    }
                    // Constant prefix of non-task items before the first task
                    // row in this list (intro + section label; the quadrant
                    // header is now a fixed bar above the list).
                    val detailTaskIndexPrefix = 2
                    val dragDropState = rememberDragDropListState(
                        lazyListState = listState,
                        onMove = { fromIndex, toIndex ->
                            val openTasks = reorderableDetailTasks.filter { !it.completed }
                            if (fromIndex in openTasks.indices && toIndex in openTasks.indices) {
                                val list = openTasks.toMutableList()
                                val item = list.removeAt(fromIndex)
                                list.add(toIndex, item)
                                reorderableDetailTasks = list + reorderableDetailTasks.filter { it.completed }
                            }
                        }
                    )
                    dragDropState.taskIndexPrefix = detailTaskIndexPrefix
                    dragDropState.openTaskCount = openDetailTaskCount

                    LaunchedEffect(dragDropState.draggedIndex) {
                        if (dragDropState.draggedIndex != null) {
                            isDragging = true
                        } else {
                            if (isDragging) {
                                scope.launch {
                                    repository.reorderTasks(reorderableDetailTasks)
                                }
                                kotlinx.coroutines.delay(50)
                                isDragging = false
                            }
                        }
                    }

                    // Prototype-first: the quadrant header is a fixed bar; only
                    // the intro/labels/task rows scroll beneath it.
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
                    ) {
                        activePriority?.let { priority ->
                            QuadrantDetailHeader(
                                quadrant = priority,
                                tasks = reorderableDetailTasks,
                                onBack = { activePriority = null }
                            )
                        }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                offset.y.toInt() in item.offset..(item.offset + item.size)
                                            }
                                            ?.let {
                                                // Subtract the constant prefix to get
                                                // the relative task index.
                                                val taskIndex = it.index - detailTaskIndexPrefix
                                                // Only open (未完成) task rows are draggable; the
                                                // completed group sits after them and must not drag.
                                                if (taskIndex in 0 until openDetailTaskCount) {
                                                    dragHaptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    dragDropState.onDragStart(taskIndex)
                                                }
                                            }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        dragDropState.onDrag(dragAmount.y)
                                    },
                                    onDragEnd = {
                                        dragDropState.onDragEnd()
                                    },
                                    onDragCancel = {
                                        dragDropState.onDragEnd()
                                    }
                                )
                            },
                        contentPadding = PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        activePriority?.let { priority ->
                            item {
                                QuadrantDetailIntro(
                                    quadrant = priority,
                                    tasks = reorderableDetailTasks
                                )
                            }
                            if (reorderableDetailTasks.isEmpty()) {
                                item {
                                    QuadrantDetailEmptyState(priority)
                                }
                            } else {
                                item {
                                    QuadrantDetailSectionLabel(
                                        text = "未完成",
                                        count = reorderableDetailTasks.count { !it.completed }
                                    )
                                }
                                itemsIndexed(
                                    reorderableDetailTasks.filter { !it.completed },
                                    key = { _, task -> task.id },
                                    contentType = { _, _ -> "open-task-row" }
                                ) { index, task ->
                                    TaskRow(
                                        task = task,
                                        repository = repository,
                                        onToggleComplete = toggleComplete,
                                        onEditTask = { t ->
                                            taskForEdit = t
                                        },
                                        onDeleteTask = { taskForDelete = it },
                                        onEditNote = { taskForNote = it },
                                        onArchiveTask = archiveTask,
                                        onViewDetail = { taskForDetail = it },
                                        showQuadrantMeta = false,
                                        isDragged = dragDropState.draggedIndex == index,
                                        dragOffsetPx = if (dragDropState.draggedIndex == index) {
                                            dragDropState.draggedOffset
                                        } else {
                                            0f
                                        },
                                        isSelectionMode = showSelectionMode,
                                        isSelected = task.id in selectedTaskIds,
                                        onLongPress = { enterSelection(task) },
                                        onToggleSelection = { toggleSelection(task) },
                                        modifier = Modifier.animateItem()
                                            // TalkBack cannot perform the
                                            // long-press drag gesture; expose
                                            // 上移/下移 custom actions that run
                                            // the same reorder + persist path.
                                            .semantics {
                                                val lastOpenIndex = openDetailTaskCount - 1
                                                val actions = mutableListOf<CustomAccessibilityAction>()
                                                if (index > 0) {
                                                    actions.add(CustomAccessibilityAction("上移") {
                                                        val openTasks = reorderableDetailTasks.filter { !it.completed }
                                                        val list = openTasks.toMutableList()
                                                        val item = list.removeAt(index)
                                                        list.add(index - 1, item)
                                                        reorderableDetailTasks = list + reorderableDetailTasks.filter { it.completed }
                                                        scope.launch { repository.reorderTasks(reorderableDetailTasks) }
                                                        true
                                                    })
                                                }
                                                if (index < lastOpenIndex) {
                                                    actions.add(CustomAccessibilityAction("下移") {
                                                        val openTasks = reorderableDetailTasks.filter { !it.completed }
                                                        val list = openTasks.toMutableList()
                                                        val item = list.removeAt(index)
                                                        list.add(index + 1, item)
                                                        reorderableDetailTasks = list + reorderableDetailTasks.filter { it.completed }
                                                        scope.launch { repository.reorderTasks(reorderableDetailTasks) }
                                                        true
                                                    })
                                                }
                                                customActions = actions
                                            }
                                    )
                                }
                                val completedDetailTasks = reorderableDetailTasks.filter { it.completed }
                                if (completedDetailTasks.isNotEmpty()) {
                                    item {
                                        QuadrantDetailCompletedGroup(
                                            tasks = completedDetailTasks,
                                            repository = repository,
                                            onToggleComplete = toggleComplete,
                                            onEditTask = { t ->
                                                taskForEdit = t
                                            },
                                            onDeleteTask = { taskForDelete = it },
                                            onEditNote = { taskForNote = it },
                                            onArchiveTask = archiveTask,
                                            onViewDetail = { taskForDetail = it }
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }
                }
                }

                // Detail overlays fade/slide in on top of the crossfaded base;
                // the snapshot keeps content alive during the exit transition.
                val detailTaskSnapshot = remember { mutableStateOf<TaskUiModel?>(null) }
                if (taskForDetail != null) detailTaskSnapshot.value = taskForDetail
                AnimatedVisibility(
                    visible = taskForDetail != null,
                    enter = (fadeIn(tween(150)) +
                        slideInVertically(tween(220)) { it / 4 })
                        .withReducedMotion(),
                    exit = (fadeOut(tween(120)) +
                        slideOutVertically(tween(180)) { it / 4 })
                        .withReducedMotion()
                ) {
                    detailTaskSnapshot.value?.let { task ->
                        TaskDetailScreen(
                            task = task,
                            repository = repository,
                            onBack = { taskForDetail = null },
                            onEdit = {
                                taskForEdit = task
                            },
                            onToggleComplete = toggleComplete,
                            onDelete = { taskForDelete = it; taskForDetail = null },
                            onEditNote = { taskForNote = it },
                            onArchive = archiveTask,
                            onOpenCalendarReminder = { openCalendarReminder(context, it) },
                            onToggleSubTask = { subTaskId, completed ->
                                scope.launch {
                                    val succeeded = snackbarHostState.runOrNotifyFailure("子事项更新失败") {
                                        repository.updateSubTaskCompleted(task.id, subTaskId, completed)
                                    }
                                    if (succeeded) {
                                        snackbarHostState.showSnackbar(if (completed) "子事项已完成" else "子事项已恢复")
                                    }
                                }
                            },
                            onDeleteCompletion = deleteCompletionWithUndo,
                            onUpdateCompletionNote = updateCompletionNoteQuietly,
                        )
                    }
                }

                AddTaskPanel(
                    visible = showAddSheet || taskForEdit != null,
                    editingTask = taskForEdit,
                    initialPriority = activePriority ?: TaskPriority.ImportantUrgent,
                    presetDueAtMillis = calendarPresetDueAtMillis,
                    onDismiss = dismissTaskPanel,
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        )
                    },
                    onSave = { form ->
                        val previousTask = taskForEdit
                        try {
                            val saved = if (previousTask == null) {
                                repository.addTask(form)
                            } else {
                                repository.updateTask(previousTask.id, form)
                            }
                            checkNotNull(saved) { "事项不存在，可能已被删除" }

                            val sideEffectFailures = mutableListOf<String>()
                            if (previousTask != null) {
                                try {
                                    scheduler.cancel(previousTask.id, previousTask.reminderLeadMinutes)
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    sideEffectFailures += "旧提醒清理失败"
                                }
                            }
                            if (saved.useLocalReminder) {
                                try {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    scheduler.schedule(saved)
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    sideEffectFailures += "到点提醒调度失败"
                                }
                            }
                            if (saved.useCalendar) {
                                try {
                                    val eventId = saveOrOpenCalendarEvent(context, saved)
                                    if (eventId != null && eventId != saved.calendarEventId) {
                                        repository.updateCalendarEventId(saved.id, eventId)
                                    }
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    sideEffectFailures += "系统日历同步失败"
                                }
                            } else {
                                try {
                                    val eventId = previousTask?.calendarEventId ?: saved.calendarEventId
                                    if (!deleteCalendarEvent(context, eventId)) {
                                        sideEffectFailures += "旧日历事件清理失败"
                                    }
                                    if (saved.calendarEventId != null) {
                                        repository.updateCalendarEventId(saved.id, null)
                                    }
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    sideEffectFailures += "旧日历事件清理失败"
                                }
                            }

                            if (sideEffectFailures.isEmpty()) {
                                TaskSaveOutcome(saved = true, message = "事项已保存")
                            } else {
                                TaskSaveOutcome(
                                    saved = true,
                                    message = "事项已保存；${sideEffectFailures.distinct().joinToString("、")}"
                                )
                            }
                        } catch (error: Exception) {
                            if (error is CancellationException) throw error
                            TaskSaveOutcome(
                                saved = false,
                                message = "保存失败：${error.message?.takeIf { it.isNotBlank() } ?: "请稍后重试"}"
                            )
                        }
                    },
                    onStatus = { message ->
                        scope.launch { snackbarHostState.showSnackbar(message) }
                    }
                )

                // Two-level settings hub and its destinations. These were
                // dropped by mistake in the pixel-parity refactor (the state
                // flags were set but nothing consumed them, so the 设置/日历
                // header buttons appeared dead). Restored here, gated on their
                // panel flags — unlike BackupPanel/ReviewScreen these screens
                // have no `visible` parameter and would otherwise render on top
                // of home unconditionally.
                if (showSettingsPanel) {
                    SettingsScreen(
                        themeMode = themeMode,
                        backupConnected = webDavSettings.isConfigured,
                        backupHost = webDavSettings.url.trim().takeIf { it.isNotEmpty() }?.let { url ->
                            runCatching { java.net.URI(url).host }.getOrNull()
                        },
                        archivedCount = archivedTasks.size,
                        completedCount = activeTasks.count { it.completed },
                        onBack = { showSettingsPanel = false },
                        onOpenAppearance = {
                            showSettingsPanel = false
                            showAppearancePanel = true
                        },
                        onOpenWidget = {
                            showSettingsPanel = false
                            returnToSettings = true
                            showWidgetPanel = true
                        },
                        onOpenBackup = {
                            showSettingsPanel = false
                            returnToSettings = true
                            showBackupPanel = true
                        },
                        onOpenArchive = {
                            showSettingsPanel = false
                            returnToSettings = true
                            showArchivedPanel = true
                        },
                        onOpenReview = {
                            showSettingsPanel = false
                            returnToSettings = true
                            showReviewPanel = true
                        },
                        onOpenAbout = { showAboutDialog = true }
                    )
                }
                if (showWidgetPanel) {
                    WidgetNotificationsScreen(
                        sampleTasks = activeTasks
                            .filter { !it.completed }
                            .sortedBy { it.dueAtMillis ?: Long.MAX_VALUE },
                        onBack = {
                            showWidgetPanel = false
                            showSettingsPanel = true
                        },
                        onOpenNotificationSettings = {
                            openAppNotificationSettings(context)
                        }
                    )
                }
                if (showAppearancePanel) {
                    AppearanceScreen(
                        themeMode = themeMode,
                        isSystemDark = isSystemInDarkTheme(),
                        onBack = {
                            showAppearancePanel = false
                            showSettingsPanel = true
                        },
                        onSelect = onThemeModeChange
                    )
                }
                BackupPanel(
                    visible = showBackupPanel,
                    initialSettings = webDavSettings,
                    autoBackupSettings = autoBackupSettings,
                    syncInProgress = backupSyncInProgress,
                    onDismiss = {
                        showBackupPanel = false
                        showSettingsPanel = returnToSettings
                        returnToSettings = false
                    },
                    onToggleAutoBackup = { enabled ->
                        backupSettingsStore.saveAutoBackupEnabled(enabled)
                        autoBackupSettings = backupSettingsStore.loadAutoBackupSettings()
                    },
                    onLocalExport = {
                        localBackupExportLauncher.launch(defaultBackupFileName())
                    },
                    onLocalImport = {
                        localBackupImportLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    onSaveWebDav = { settings ->
                        webDavSettings = settings
                        backupSettingsStore.saveWebDavSettings(settings)
                        scope.launch { snackbarHostState.showSnackbar("WebDAV 设置已保存") }
                    },
                    onUploadWebDav = { settings ->
                        scope.launch {
                            backupSyncInProgress = true
                            try {
                                runCatching {
                                    backupSettingsStore.saveWebDavSettings(settings)
                                    webDavSettings = settings
                                    webDavClient.upload(settings, repository.exportBackupJson())
                                }.onSuccess {
                                    snackbarHostState.showSnackbar("WebDAV 备份已上传")
                                }.onFailure { error ->
                                    snackbarHostState.showSnackbar("上传失败：${error.message.orEmpty()}")
                                }
                            } finally {
                                backupSyncInProgress = false
                            }
                        }
                    },
                    onRestoreWebDav = { settings ->
                        scope.launch {
                            backupSyncInProgress = true
                            try {
                                runCatching {
                                    backupSettingsStore.saveWebDavSettings(settings)
                                    webDavSettings = settings
                                    // Safety net before the destructive
                                    // import: snapshot the current local
                                    // data next to the backup on the same
                                    // WebDAV share, so a wrong or corrupt
                                    // restore source can be undone by
                                    // restoring this snapshot. Aborting on
                                    // snapshot failure — restoring without
                                    // an undo path is worse than waiting.
                                    try {
                                        webDavClient.upload(
                                            settings,
                                            repository.exportBackupJson(),
                                            WEBDAV_PRE_RESTORE_SNAPSHOT_FILE_NAME
                                        )
                                    } catch (error: Exception) {
                                        if (error is CancellationException) throw error
                                        throw IOException(
                                            "恢复前快照上传失败（${error.message.orEmpty().take(120)}），本地数据未被改动",
                                            error
                                        )
                                    }
                                    val count = repository.importBackupJson(webDavClient.download(settings))
                                    repository.restoreScheduledReminders().forEach { task ->
                                        scheduler.schedule(task)
                                    }
                                    count
                                }.onSuccess { count ->
                                    snackbarHostState.showSnackbar("已从 WebDAV 恢复 $count 个事项")
                                }.onFailure { error ->
                                    snackbarHostState.showSnackbar("恢复失败：${error.message.orEmpty()}")
                                }
                            } finally {
                                backupSyncInProgress = false
                            }
                        }
                    }
                )
                ReviewScreen(
                    events = reviewEvents,
                    stats = reviewStats,
                    onBack = {
                        showReviewPanel = false
                        showSettingsPanel = returnToSettings
                        returnToSettings = false
                    },
                    visible = showReviewPanel
                )
                CalendarMonthScreen(
                    visible = showCalendarView,
                    tasks = activeTasks,
                    nowMillis = nowMillis,
                    zoneId = zoneId,
                    onBack = { showCalendarView = false },
                    onTaskSelected = { taskForDetail = it },
                    onQuickAdd = { date ->
                        calendarPresetDueAtMillis = date.atStartOfDay(zoneId).toInstant().toEpochMilli()
                        showAddSheet = true
                    },
                    onToggleComplete = toggleComplete,
                    onMoveTask = { task, dateMillis -> moveTaskDueToDate(task, dateMillis) }
                )

                ArchivedPanel(
                    visible = showArchivedPanel,
                    tasks = archivedTasks,
                    onDismiss = {
                        showArchivedPanel = false
                        showSettingsPanel = returnToSettings
                        returnToSettings = false
                    },
                    onUnarchive = { task ->
                        scope.launch {
                            val succeeded = snackbarHostState.runOrNotifyFailure("恢复失败") {
                                repository.unarchiveTask(task.id)
                            }
                            if (succeeded) {
                                snackbarHostState.showSnackbar("事项已恢复")
                            }
                        }
                    }
                )
            }
        }

        // Batch-selection bottom action bar: appears over the content when
        // multi-select is active. Rendered above the Surface so it floats over
        // the task list regardless of the underlying page.
        if (showSelectionMode) {
            BatchSelectionBar(
                selectedCount = selectedTaskIds.size,
                onMoveQuadrant = { batchMoveQuadrantTarget = true },
                onDelete = { batchDeleteConfirm = true }
            )
        }
    }

    if (batchMoveQuadrantTarget) {
        BatchMoveQuadrantDialog(
            onDismiss = { batchMoveQuadrantTarget = false },
            onConfirm = { priority ->
                batchMoveQuadrantTarget = false
                batchMoveQuadrant(priority)
            }
        )
    }

    if (batchDeleteConfirm) {
        BatchDeleteConfirmDialog(
            count = selectedTaskIds.size,
            onDismiss = { batchDeleteConfirm = false },
            onConfirm = {
                batchDeleteConfirm = false
                batchDelete()
            }
        )
    }

    if (showAboutDialog) {
        AboutDialog(onDismiss = { showAboutDialog = false })
    }

        // Global notice overlay: fixed top anchoring on every page. The
        // Scaffold's own snackbar slot shifts with FAB presence, so the host
        // is drawn here instead — above all content, unaffected by the FAB.
        TopNoticeHost(snackbarHostState)
    }

    taskForNote?.let { task ->
        CompletionNoteDialog(
            task = task,
            onDismiss = { taskForNote = null },
            onSave = { note ->
                scope.launch {
                    snackbarHostState.runOrNotifyFailure("完成失败") {
                        val updated = repository.saveCompletionNote(task.id, note)
                        // Completing a non-recurring task settles it — drop the
                        // pending local reminder; recurring tasks reschedule.
                        if (!task.completed) {
                            scheduler.cancel(task.id, task.reminderLeadMinutes)
                            if (updated != null && updated.useLocalReminder && updated.dueAtMillis != null) {
                                scheduler.schedule(updated.toUiModel())
                            }
                        }
                        snackbarHostState.showSnackbar("完成心得已保存")
                    }
                }
                taskForNote = null
            }
        )
    }

    taskForDelete?.let { task ->
        DeleteTaskDialog(
            task = task,
            onDismiss = { taskForDelete = null },
            onConfirm = {
                scope.launch {
                    snackbarHostState.runOrNotifyFailure("删除失败") {
                        scheduler.cancel(task.id, task.reminderLeadMinutes)
                        // Remove immediately so the row disappears, then wait
                        // for the undo window before making it permanent.
                        val removed = repository.deleteTask(task.id)
                        val result = snackbarHostState.showSnackbar(
                            message = "事项已删除",
                            actionLabel = "撤销",
                            duration = SnackbarDuration.Long
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            // Undo: re-insert the same entity (same id, order,
                            // fields); the calendar event was never touched.
                            removed?.let { entity ->
                                repository.restoreTask(entity)
                                scheduler.schedule(entity.toUiModel())
                            }
                        } else {
                            // Undo window closed: clean up the linked calendar
                            // event for good.
                            deleteCalendarEvent(context, task.calendarEventId)
                        }
                    }
                }
                taskForDelete = null
            }
        )
    }

    taskForUncompletion?.let { task ->
        UncompleteRecurringTaskDialog(
            onDismiss = { taskForUncompletion = null },
            onConfirm = {
                scope.launch {
                    val succeeded = snackbarHostState.runOrNotifyFailure("恢复失败") {
                        repository.toggleCompleted(task, false)
                        if (task.useLocalReminder && task.dueAtMillis != null) {
                            scheduler.schedule(task)
                        }
                    }
                    if (succeeded) {
                        snackbarHostState.showSnackbar("事项已恢复为未完成")
                    }
                }
                taskForUncompletion = null
            }
        )
    }
}

/**
 * Standard error guard for user-triggered repository actions: cancellation
 * propagates (it means the scope died, not a real failure) and any other
 * exception shows the standard「<contextMessage>：<原因>」notice. Returns true
 * when [block] completed, so callers can append success-only side effects
 * (confirmation snackbar、退出批量选择等) without nesting another try/catch.
 */
private suspend fun SnackbarHostState.runOrNotifyFailure(
    contextMessage: String,
    block: suspend () -> Unit
): Boolean = try {
    block()
    true
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    showSnackbar("$contextMessage：${error.message ?: "请稍后重试"}")
    false
}

/**
 * Fixed top-anchored global notice host.
 *
 * Drawn as an overlay above the whole app instead of the Scaffold's
 * snackbar slot: the M3 Scaffold anchors its snackbar slot from the bottom
 * and raises it to avoid the FAB, which only exists on the home surface —
 * so a slot host would land at different heights per page. Anchoring here
 * keeps the notice at the same spot everywhere, and tapping the card
 * dismisses it immediately.
 */
@Composable
private fun TopNoticeHost(hostState: SnackbarHostState) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(top = 72.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        SnackbarHost(hostState) { data ->
            val isError = data.visuals.message.contains("失败")
            val toastTint = if (isError) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.secondary
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClick = { data.dismiss() }
                    ),
                shape = RowShape,
                color = MaterialTheme.colorScheme.surface,
                border = quietBorder(0.16f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconTile(
                        icon = if (isError) DianMaoIcons.Close else DianMaoIcons.CheckCircle,
                        contentDescription = null,
                        tint = toastTint,
                        containerColor = toastTint.copy(alpha = 0.10f),
                        iconSize = 15.dp
                    )
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (data.visuals.actionLabel != null) {
                        TextButton(onClick = { data.performAction() }) {
                            Text(
                                text = data.visuals.actionLabel.orEmpty(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Prototype selection header: close + "已选 N 项" + 全选, replacing the normal
 * app header while multi-select is active (.app-header.scaffold branch in
 * render()).
 */
@Composable
private fun SelectionHeader(
    selectedCount: Int,
    allSelected: Boolean,
    onExit: () -> Unit,
    onToggleAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        HeaderActionButton(
            icon = DianMaoIcons.Close,
            contentDescription = "退出多选",
            onClick = onExit
        )
        Text(
            text = "已选 $selectedCount 项",
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        TextButton(onClick = onToggleAll) {
            Text(if (allSelected) "取消全选" else "全选")
        }
    }
}

/**
 * Prototype .select-bar: a sticky two-button row at the bottom of the content
 * while multi-select is active — 移动象限 (tonal) and 删除所选 (error tint).
 * There is deliberately no 完成 button or shadowed bottom sheet in the
 * prototype.
 */
@Composable
private fun BatchSelectionBar(
    selectedCount: Int,
    onMoveQuadrant: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        FilledTonalButton(
            onClick = onMoveQuadrant,
            enabled = selectedCount > 0,
            modifier = Modifier.weight(1f),
            shape = RowShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
        ) {
            InlineButtonIcon(
                icon = DianMaoIcons.Inventory,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
            )
            Spacer(Modifier.width(7.dp))
            Text("移动象限")
        }
        Button(
            onClick = onDelete,
            enabled = selectedCount > 0,
            modifier = Modifier.weight(1f),
            shape = RowShape,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                contentColor = MaterialTheme.colorScheme.error
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
        ) {
            InlineButtonIcon(
                icon = DianMaoIcons.Delete,
                tint = MaterialTheme.colorScheme.error,
                containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
            )
            Spacer(Modifier.width(7.dp))
            Text("删除所选")
        }
    }
}
