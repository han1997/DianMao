package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.alerts.ReminderScheduler
import `fun`.han1997.dianmao.alerts.TaskNotificationChannels
import `fun`.han1997.dianmao.alerts.TaskReminderReceiver
import `fun`.han1997.dianmao.backup.AutoBackupSettings
import `fun`.han1997.dianmao.backup.BackupSettingsStore
import `fun`.han1997.dianmao.backup.WebDavBackupClient
import `fun`.han1997.dianmao.backup.shouldAutoBackupNow
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskEntity
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.toUiModel
import `fun`.han1997.dianmao.TaskRow
import `fun`.han1997.dianmao.rememberDragDropListState
import `fun`.han1997.dianmao.HomeSmartView
import `fun`.han1997.dianmao.InAppBackTarget
import `fun`.han1997.dianmao.buildHomeSmartTaskItems
import `fun`.han1997.dianmao.resolveInAppBackTarget
import `fun`.han1997.dianmao.TaskSaveOutcome
import `fun`.han1997.dianmao.defaultBackupFileName
import `fun`.han1997.dianmao.deleteCalendarEvent
import `fun`.han1997.dianmao.openCalendarReminder
import `fun`.han1997.dianmao.readTextFromUri
import `fun`.han1997.dianmao.saveOrOpenCalendarEvent
import `fun`.han1997.dianmao.seedTaskEntities
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
import `fun`.han1997.dianmao.RecurringTaskHistoryPanel
import `fun`.han1997.dianmao.TaskDetailScreen
import `fun`.han1997.dianmao.DianMaoTheme
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        TaskNotificationChannels.ensure(this)

        setContent {
            DianMaoTheme {
                DianMaoApp(initialTaskId = extractReminderTaskId(intent))
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
    }

    private fun extractReminderTaskId(intent: Intent?): Long? {
        if (intent == null || !intent.hasExtra(TaskReminderReceiver.EXTRA_TASK_ID)) return null
        val id = intent.getLongExtra(TaskReminderReceiver.EXTRA_TASK_ID, 0L)
        return if (id > 0L) id else null
    }

    companion object {
        // Set from composition so onNewIntent can forward deep-link task ids
        // into the Compose tree without a shared ViewModel.
        @Volatile
        var currentComposeTaskId: ((Long) -> Unit)? = null
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DianMaoApp(initialTaskId: Long? = null) {
    val context = LocalContext.current
    val repository = remember(context) { TaskRepository(context.applicationContext) }
    val scheduler = remember(context) { ReminderScheduler(context.applicationContext) }
    val backupSettingsStore = remember(context) { BackupSettingsStore(context.applicationContext) }
    val webDavClient = remember { WebDavBackupClient() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeTasks by repository.activeTasks.collectAsState(initial = emptyList())
    val archivedTasks by repository.archivedTasks.collectAsState(initial = emptyList())

    var activePriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showBackupPanel by rememberSaveable { mutableStateOf(false) }
    var backupSyncInProgress by remember { mutableStateOf(false) }
    var showArchivedPanel by rememberSaveable { mutableStateOf(false) }
    var taskForNote by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForEdit by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForHistory by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForDelete by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForUncompletion by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForDetail by remember { mutableStateOf<TaskUiModel?>(null) }
    var homeSmartView by rememberSaveable { mutableStateOf(HomeSmartView.Quadrants) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
    var homeSearchVisible by rememberSaveable { mutableStateOf(false) }
    val homeSmartItems = remember(activeTasks, homeSmartView, homeSearchQuery) {
        buildHomeSmartTaskItems(
            tasks = activeTasks,
            view = homeSmartView,
            query = homeSearchQuery
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
        onDispose { MainActivity.currentComposeTaskId = null }
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
        repository.seedIfEmpty(seedTaskEntities())
        repository.resetExpiredCycleTasks()
        // Re-arm local reminders after process start (covers reboot-less kills;
        // reboot itself is handled by ReminderRestoreReceiver).
        repository.restoreScheduledReminders().forEach { task ->
            scheduler.schedule(task)
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
                    try {
                        repository.toggleCompleted(target, false)
                        if (target.useLocalReminder && target.dueAtMillis != null) {
                            scheduler.schedule(target)
                        }
                        snackbarHostState.showSnackbar("事项已标为未完成")
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        snackbarHostState.showSnackbar("标记失败：${error.message ?: "请稍后重试"}")
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
            try {
                repository.completeSilently(target)
                if (target.useLocalReminder && target.dueAtMillis != null) {
                    scheduler.cancel(target.id, target.reminderLeadMinutes)
                    if (target.repeatRule != RepeatRule.None) {
                        scheduler.schedule(target)
                    }
                }
                val result = snackbarHostState.showSnackbar(
                    message = "已完成「${target.title}」",
                    actionLabel = "写心得",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    taskForNote = target
                }
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                snackbarHostState.showSnackbar("完成失败：${error.message ?: "请稍后重试"}")
            }
        }
    }

    val archiveTask: (TaskUiModel) -> Unit = { target ->
        scope.launch {
            try {
                repository.archiveTask(target.id)
                snackbarHostState.showSnackbar("事项已归档")
            } catch (error: Exception) {
                if (error is CancellationException) throw error
                snackbarHostState.showSnackbar("归档失败：${error.message ?: "请稍后重试"}")
            }
        }
    }

    val isTaskPanelVisible = showAddSheet || taskForEdit != null
    val dismissTaskPanel = {
        showAddSheet = false
        taskForEdit = null
    }
    val backTarget = resolveInAppBackTarget(
        showBackupPanel = showBackupPanel,
        showArchivedPanel = showArchivedPanel,
        isTaskPanelVisible = isTaskPanelVisible,
        hasActivePriority = activePriority != null,
        taskForDetail = taskForDetail != null
    )

    BackHandler(enabled = backTarget != null) {
        when (backTarget) {
            InAppBackTarget.BackupPanel -> showBackupPanel = false
            InAppBackTarget.ArchivedPanel -> showArchivedPanel = false
            InAppBackTarget.TaskPanel -> dismissTaskPanel()
            InAppBackTarget.TaskDetail -> taskForDetail = null
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
                // overlay panel is open — the FAB lives above the content
                // layer and would otherwise float over the panel.
                if (taskForDetail == null &&
                    !showBackupPanel && !showArchivedPanel && !isTaskPanelVisible
                ) {
                    FloatingActionButton(
                        onClick = { showAddSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        shape = IconTileShape
                    ) {
                        Icon(
                            Icons.Rounded.Add,
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
                            onOpenBackup = {
                                showBackupPanel = true
                            },
                            onOpenArchived = {
                                showArchivedPanel = true
                            }
                        )
                        HomeSmartViewSwitcher(
                            selected = homeSmartView,
                            onSelect = { homeSmartView = it }
                        )
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
                                onTaskSelected = { taskForDetail = it }
                            )
                            QuadrantBoard(
                                tasks = homeVisibleTasks,
                                modifier = Modifier.weight(1f),
                                onSelected = { priority -> activePriority = priority },
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
                                onToggleComplete = toggleComplete,
                                onCompleteSilently = completeSilently,
                                onEditTask = { taskForEdit = it },
                                onDeleteTask = { taskForDelete = it },
                                onEditNote = { taskForNote = it },
                                onArchiveTask = archiveTask,
                                onViewDetail = { taskForDetail = it },
                                onAddTask = { showAddSheet = true },
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
                    // row in this list (header + intro + section label).
                    val detailTaskIndexPrefix = 3
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

                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .windowInsetsPadding(WindowInsets.safeDrawing)
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
                                QuadrantDetailHeader(
                                    quadrant = priority,
                                    tasks = reorderableDetailTasks,
                                    onBack = { activePriority = null }
                                )
                            }
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
                                try {
                                    repository.updateSubTaskCompleted(task.id, subTaskId, completed)
                                    snackbarHostState.showSnackbar(if (completed) "子事项已完成" else "子事项已恢复")
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    snackbarHostState.showSnackbar("子事项更新失败：${error.message ?: "请稍后重试"}")
                                }
                            }
                        },
                        onDeleteCompletion = { completionId ->
                            scope.launch {
                                try {
                                    val removed = repository.deleteCompletionForUndo(completionId)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "完成记录已删除",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        removed?.let { repository.restoreCompletion(it) }
                                    }
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    snackbarHostState.showSnackbar("删除完成记录失败：${error.message ?: "请稍后重试"}")
                                }
                            }
                        },
                        onUpdateCompletionNote = { completionId, note ->
                            scope.launch {
                                try {
                                    repository.updateCompletionNote(completionId, note)
                                    snackbarHostState.showSnackbar("完成心得已保存")
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    snackbarHostState.showSnackbar("保存完成心得失败：${error.message ?: "请稍后重试"}")
                                }
                            }
                        },
                        onOpenHistory = { taskForHistory = it }
                    )
                    }
                }

                AddTaskPanel(
                    visible = showAddSheet || taskForEdit != null,
                    editingTask = taskForEdit,
                    initialPriority = activePriority ?: TaskPriority.ImportantUrgent,
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

                val historyTaskSnapshot = remember { mutableStateOf<TaskUiModel?>(null) }
                if (taskForHistory != null) historyTaskSnapshot.value = taskForHistory
                AnimatedVisibility(
                    visible = taskForHistory != null,
                    enter = (fadeIn(tween(150)) +
                        slideInVertically(tween(220)) { it / 4 })
                        .withReducedMotion(),
                    exit = (fadeOut(tween(120)) +
                        slideOutVertically(tween(180)) { it / 4 })
                        .withReducedMotion()
                ) {
                    historyTaskSnapshot.value?.let { task ->
                    RecurringTaskHistoryPanel(
                        task = task,
                        repository = repository,
                        onDismiss = { taskForHistory = null },
                        onEdit = { t ->
                            taskForHistory = null
                            taskForEdit = t
                        },
                        onComplete = { taskForNote = it },
                        onDeleteCompletion = { completionId ->
                            scope.launch {
                                try {
                                    val removed = repository.deleteCompletionForUndo(completionId)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "完成记录已删除",
                                        actionLabel = "撤销",
                                        duration = SnackbarDuration.Long
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        removed?.let { repository.restoreCompletion(it) }
                                    }
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    snackbarHostState.showSnackbar("删除完成记录失败：${error.message ?: "请稍后重试"}")
                                }
                            }
                        },
                        onUpdateCompletionNote = { completionId, note ->
                            scope.launch {
                                try {
                                    repository.updateCompletionNote(completionId, note)
                                    snackbarHostState.showSnackbar("完成心得已保存")
                                } catch (error: Exception) {
                                    if (error is CancellationException) throw error
                                    snackbarHostState.showSnackbar("保存完成心得失败：${error.message ?: "请稍后重试"}")
                                }
                            }
                        }
                    )
                    }
                }
                BackupPanel(
                    visible = showBackupPanel,
                    initialSettings = webDavSettings,
                    autoBackupSettings = autoBackupSettings,
                    syncInProgress = backupSyncInProgress,
                    onDismiss = { showBackupPanel = false },
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
                ArchivedPanel(
                    visible = showArchivedPanel,
                    tasks = archivedTasks,
                    onDismiss = { showArchivedPanel = false },
                    onUnarchive = { task ->
                        scope.launch {
                            try {
                                repository.unarchiveTask(task.id)
                                snackbarHostState.showSnackbar("事项已恢复")
                            } catch (error: Exception) {
                                if (error is CancellationException) throw error
                                snackbarHostState.showSnackbar("恢复失败：${error.message ?: "请稍后重试"}")
                            }
                        }
                    }
                )
            }
        }
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
                    try {
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
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        snackbarHostState.showSnackbar("完成失败：${error.message ?: "请稍后重试"}")
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
                try {
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
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        snackbarHostState.showSnackbar("删除失败：${error.message ?: "请稍后重试"}")
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
                    try {
                        repository.toggleCompleted(task, false)
                        if (task.useLocalReminder && task.dueAtMillis != null) {
                            scheduler.schedule(task)
                        }
                        snackbarHostState.showSnackbar("事项已恢复为未完成")
                    } catch (error: Exception) {
                        if (error is CancellationException) throw error
                        snackbarHostState.showSnackbar("恢复失败：${error.message ?: "请稍后重试"}")
                    }
                }
                taskForUncompletion = null
            }
        )
    }
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
                        icon = if (isError) Icons.Rounded.Close else Icons.Rounded.CheckCircle,
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
