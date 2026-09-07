package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.backup.BackupSettingsStore
import `fun`.han1997.dianmao.backup.WebDavBackupClient
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskEntity
import `fun`.han1997.dianmao.data.TaskUiModel
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
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            DianMaoTheme {
                DianMaoApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DianMaoApp() {
    val context = LocalContext.current
    val repository = remember(context) { TaskRepository(context.applicationContext) }
    val backupSettingsStore = remember(context) { BackupSettingsStore(context.applicationContext) }
    val webDavClient = remember { WebDavBackupClient() }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val activeTasks by repository.activeTasks.collectAsState(initial = emptyList())
    val archivedTasks by repository.archivedTasks.collectAsState(initial = emptyList())

    var activePriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showBackupPanel by rememberSaveable { mutableStateOf(false) }
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
    var webDavSettings by remember { mutableStateOf(backupSettingsStore.loadWebDavSettings()) }
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
                    count
                }.onSuccess { count ->
                    snackbarHostState.showSnackbar("已从本地备份恢复 $count 个事项")
                }.onFailure { error ->
                    snackbarHostState.showSnackbar("恢复失败：${error.message.orEmpty()}")
                }
            }
        }
    }

    LaunchedEffect(repository) {
        repository.seedIfEmpty(seedTaskEntities())
        repository.resetExpiredCycleTasks()
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
                // Home surface only, and never while an overlay panel is
                // open — the FAB lives above the content layer and would
                // otherwise float over the panel and block its content.
                if (activePriority == null && taskForDetail == null &&
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
                if (activePriority == null) {
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
                    val listState = rememberLazyListState()
                    val openDetailTaskCount = reorderableDetailTasks.count { !it.completed }
                    val dragDropState = rememberDragDropListState(
                        lazyListState = listState,
                        onMove = { fromIndex, toIndex ->
                            if (fromIndex in 0 until openDetailTaskCount &&
                                toIndex in 0 until openDetailTaskCount
                            ) {
                                val openTasks = reorderableDetailTasks.filter { !it.completed }
                                val list = openTasks.toMutableList()
                                val item = list.removeAt(fromIndex)
                                list.add(toIndex, item)
                                reorderableDetailTasks = list + reorderableDetailTasks.filter { it.completed }
                            }
                        }
                    )

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
                                                // Subtract 3 to account for header, intro, and section label items
                                                val taskIndex = it.index - 3
                                                // Only open (未完成) task rows are draggable; the
                                                // completed group sits after them and must not drag.
                                                if (taskIndex in 0 until openDetailTaskCount) {
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
                                    key = { _, task -> task.id }
                                ) { _, task ->
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
                                        showQuadrantMeta = false
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

                taskForDetail?.let { task ->
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
                        }
                    )
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

                taskForHistory?.let { task ->
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

                BackupPanel(
                    visible = showBackupPanel,
                    initialSettings = webDavSettings,
                    onDismiss = { showBackupPanel = false },
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
                            runCatching {
                                backupSettingsStore.saveWebDavSettings(settings)
                                webDavSettings = settings
                                webDavClient.upload(settings, repository.exportBackupJson())
                            }.onSuccess {
                                snackbarHostState.showSnackbar("WebDAV 备份已上传")
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar("上传失败：${error.message.orEmpty()}")
                            }
                        }
                    },
                    onRestoreWebDav = { settings ->
                        scope.launch {
                            runCatching {
                                backupSettingsStore.saveWebDavSettings(settings)
                                webDavSettings = settings
                                val count = repository.importBackupJson(webDavClient.download(settings))
                                count
                            }.onSuccess { count ->
                                snackbarHostState.showSnackbar("已从 WebDAV 恢复 $count 个事项")
                            }.onFailure { error ->
                                snackbarHostState.showSnackbar("恢复失败：${error.message.orEmpty()}")
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
                        repository.saveCompletionNote(task.id, note)
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
                            removed?.let { repository.restoreTask(it) }
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
