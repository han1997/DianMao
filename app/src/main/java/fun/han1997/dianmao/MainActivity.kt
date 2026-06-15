package `fun`.han1997.dianmao

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import `fun`.han1997.dianmao.alerts.ReminderScheduler
import `fun`.han1997.dianmao.alerts.TaskNotificationChannels
import `fun`.han1997.dianmao.backup.BackupSettingsStore
import `fun`.han1997.dianmao.backup.WebDavBackupClient
import `fun`.han1997.dianmao.backup.WebDavSettings
import `fun`.han1997.dianmao.calendar.CalendarWriter
import `fun`.han1997.dianmao.data.QuadrantType
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskEntity
import `fun`.han1997.dianmao.data.TaskFormState
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        TaskNotificationChannels.ensure(this)

        setContent {
            DianMaoTheme {
                DianMaoApp()
            }
        }
    }
}

private val TaskPriority.color: Color
    get() = Color(colorInt())

private val TaskPriority.title: String
    get() = label

private val TaskPriority.shortTitle: String
    get() = shortLabel

private val TaskPriority.type: QuadrantType
    get() = QuadrantType.valueOf(name)

private val QuadrantType.priority: TaskPriority
    get() = TaskPriority.valueOf(name)

private fun hasCalendarPermission(context: android.content.Context): Boolean {
    return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
}

private fun saveOrOpenCalendarEvent(context: android.content.Context, task: TaskUiModel): Long? {
    if (hasCalendarPermission(context)) {
        CalendarWriter.saveOrUpdateEvent(context, task)?.let { return it }
    }

    if (task.useCalendar) {
        CalendarWriter.buildInsertIntent(task)?.let { context.startActivity(it) }
    }
    return null
}

private fun deleteCalendarEvent(context: android.content.Context, eventId: Long?) {
    if (eventId != null && hasCalendarPermission(context)) {
        CalendarWriter.deleteEvent(context, eventId)
    }
}

private fun defaultBackupFileName(): String {
    val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    return "dianmao-backup-$stamp.json"
}

private suspend fun writeTextToUri(context: android.content.Context, uri: Uri, text: String) {
    withContext(Dispatchers.IO) {
        context.contentResolver.openOutputStream(uri)?.bufferedWriter(Charsets.UTF_8)?.use { writer ->
            writer.write(text)
        } ?: error("无法打开备份文件")
    }
}

private suspend fun readTextFromUri(context: android.content.Context, uri: Uri): String {
    return withContext(Dispatchers.IO) {
        context.contentResolver.openInputStream(uri)?.bufferedReader(Charsets.UTF_8)?.use { reader ->
            reader.readText()
        } ?: error("无法读取备份文件")
    }
}

private suspend fun restoreReminderSchedules(repository: TaskRepository, scheduler: ReminderScheduler) {
    repository.restoreScheduledReminders().forEach { task ->
        scheduler.schedule(task)
    }
}

private fun seedTaskEntities(): List<TaskEntity> {
    val now = System.currentTimeMillis()
    val today18 = LocalDate.now()
        .atTime(18, 0)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    val tomorrow930 = LocalDate.now()
        .plusDays(1)
        .atTime(9, 30)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    return listOf(
        TaskEntity(
            title = "梳理本周必须交付的工作",
            quadrant = QuadrantType.ImportantUrgent,
            dueLabel = "今天 18:00",
            dueAtMillis = today18,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = false,
            completionNote = null,
            createdAtMillis = now - 3_000,
            updatedAtMillis = now - 3_000
        ),
        TaskEntity(
            title = "设计四象限任务模型",
            quadrant = QuadrantType.ImportantNotUrgent,
            dueLabel = "长期",
            dueAtMillis = null,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = false,
            completionNote = null,
            createdAtMillis = now - 2_000,
            updatedAtMillis = now - 2_000
        ),
        TaskEntity(
            title = "确认明早会议提醒",
            quadrant = QuadrantType.NotImportantUrgent,
            dueLabel = "明天 09:30",
            dueAtMillis = tomorrow930,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = false,
            completionNote = null,
            createdAtMillis = now - 1_000,
            updatedAtMillis = now - 1_000
        ),
        TaskEntity(
            title = "整理完成任务心得",
            quadrant = QuadrantType.NotImportantNotUrgent,
            dueLabel = null,
            dueAtMillis = null,
            useCalendar = false,
            useAlarm = false,
            repeatRule = RepeatRule.None,
            completed = true,
            completionNote = "完成后记录判断依据，方便以后复盘同类任务。",
            createdAtMillis = now,
            updatedAtMillis = now
        )
    )
}
@Composable
private fun DianMaoTheme(content: @Composable () -> Unit) {
    val colorScheme = androidx.compose.material3.lightColorScheme(
        primary = Color(0xFFA94332),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFF8DED8),
        onPrimaryContainer = Color(0xFF3A0B05),
        secondary = Color(0xFF2D6DA8),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFDCE8F5),
        onSecondaryContainer = Color(0xFF071E34),
        background = Color(0xFFF7F8F6),
        onBackground = Color(0xFF1D211F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1D211F),
        surfaceVariant = Color(0xFFECEFED),
        onSurfaceVariant = Color(0xFF58615D),
        outline = Color(0xFFD1D7D3)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 27.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.SemiBold
            ),
            titleLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold
            ),
            titleMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold
            ),
            bodyLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            ),
            labelMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            labelLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium
            )
        ),
        content = content
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DianMaoApp() {
    val context = LocalContext.current
    val repository = remember(context) { TaskRepository(context.applicationContext) }
    val scheduler = remember(context) { ReminderScheduler(context.applicationContext) }
    val backupSettingsStore = remember(context) { BackupSettingsStore(context.applicationContext) }
    val webDavClient = remember { WebDavBackupClient() }
    val scope = rememberCoroutineScope()
    val tasks by repository.tasks.collectAsState(initial = emptyList())
    var activePriority by rememberSaveable { mutableStateOf<TaskPriority?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var showBackupPanel by rememberSaveable { mutableStateOf(false) }
    var taskForNote by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForEdit by remember { mutableStateOf<TaskUiModel?>(null) }
    var taskForDelete by remember { mutableStateOf<TaskUiModel?>(null) }
    var backupStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var webDavSettings by remember { mutableStateOf(backupSettingsStore.loadWebDavSettings()) }
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
                backupStatus = "正在导出本地备份"
                runCatching {
                    writeTextToUri(context, uri, repository.exportBackupJson())
                }.onSuccess {
                    backupStatus = "本地备份已导出"
                }.onFailure { error ->
                    backupStatus = "导出失败：${error.message.orEmpty()}"
                }
            }
        }
    }
    val localBackupImportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                backupStatus = "正在恢复本地备份"
                runCatching {
                    val count = repository.importBackupJson(readTextFromUri(context, uri))
                    restoreReminderSchedules(repository, scheduler)
                    count
                }.onSuccess { count ->
                    backupStatus = "已从本地备份恢复 $count 个事项"
                }.onFailure { error ->
                    backupStatus = "恢复失败：${error.message.orEmpty()}"
                }
            }
        }
    }

    LaunchedEffect(repository, scheduler) {
        repository.seedIfEmpty(seedTaskEntities())
        restoreReminderSchedules(repository, scheduler)
    }

    val activeTasks = remember(tasks, activePriority) {
        activePriority?.let { priority -> tasks.filter { it.quadrant == priority } }.orEmpty()
    }
    val toggleComplete: (TaskUiModel) -> Unit = { target ->
        if (target.completed) {
            scope.launch {
                repository.toggleCompleted(target, false)
                scheduler.cancel(target.id)
            }
        } else {
            taskForNote = target
        }
    }

    if (activePriority != null) {
        BackHandler {
            activePriority = null
        }
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showAddSheet = true },
                    icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                    text = { Text("新增事项") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(18.dp)
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                if (activePriority == null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(start = 18.dp, top = 6.dp, end = 18.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AppHeader(
                            tasks = tasks,
                            onOpenBackup = {
                                backupStatus = null
                                showBackupPanel = true
                            }
                        )
                        QuadrantBoard(
                            tasks = tasks,
                            modifier = Modifier.weight(1f),
                            onSelected = { priority -> activePriority = priority }
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 18.dp, top = 6.dp, end = 18.dp, bottom = 112.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        activePriority?.let { priority ->
                            item {
                                QuadrantDetailHeader(
                                    quadrant = priority,
                                    tasks = activeTasks,
                                    onBack = { activePriority = null }
                                )
                            }
                            if (activeTasks.isEmpty()) {
                                item {
                                    QuadrantDetailEmptyState(priority)
                                }
                            } else {
                                items(activeTasks, key = { it.id }) { task ->
                                    TaskRow(
                                        task = task,
                                        onToggleComplete = toggleComplete,
                                        onEditTask = { taskForEdit = it },
                                        onDeleteTask = { taskForDelete = it },
                                        onEditNote = { taskForNote = it }
                                    )
                                }
                            }
                        }
                    }
                }

                AddTaskPanel(
                    visible = showAddSheet || taskForEdit != null,
                    editingTask = taskForEdit,
                    initialPriority = activePriority ?: TaskPriority.ImportantUrgent,
                    onDismiss = {
                        showAddSheet = false
                        taskForEdit = null
                    },
                    onRequestCalendarPermission = {
                        calendarPermissionLauncher.launch(
                            arrayOf(Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR)
                        )
                    },
                    onSave = { form ->
                        scope.launch {
                            val previousTask = taskForEdit
                            val saved = if (previousTask == null) {
                                repository.addTask(form)
                            } else {
                                scheduler.cancel(previousTask.id)
                                repository.updateTask(previousTask.id, form)
                            }
                            if (saved != null) {
                                if (saved.useAlarm) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    }
                                    scheduler.schedule(saved)
                                }

                                if (saved.useCalendar) {
                                    val eventId = saveOrOpenCalendarEvent(context, saved)
                                    if (eventId != null && eventId != saved.calendarEventId) {
                                        repository.updateCalendarEventId(saved.id, eventId)
                                    }
                                } else {
                                    val eventId = previousTask?.calendarEventId ?: saved.calendarEventId
                                    deleteCalendarEvent(context, eventId)
                                    if (saved.calendarEventId != null) {
                                        repository.updateCalendarEventId(saved.id, null)
                                    }
                                }
                            }
                        }
                        showAddSheet = false
                        taskForEdit = null
                    }
                )
                BackupPanel(
                    visible = showBackupPanel,
                    initialSettings = webDavSettings,
                    status = backupStatus,
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
                        backupStatus = "WebDAV 设置已保存"
                    },
                    onUploadWebDav = { settings ->
                        scope.launch {
                            backupStatus = "正在上传 WebDAV 备份"
                            runCatching {
                                backupSettingsStore.saveWebDavSettings(settings)
                                webDavSettings = settings
                                webDavClient.upload(settings, repository.exportBackupJson())
                            }.onSuccess {
                                backupStatus = "WebDAV 备份已上传"
                            }.onFailure { error ->
                                backupStatus = "上传失败：${error.message.orEmpty()}"
                            }
                        }
                    },
                    onRestoreWebDav = { settings ->
                        scope.launch {
                            backupStatus = "正在从 WebDAV 恢复"
                            runCatching {
                                backupSettingsStore.saveWebDavSettings(settings)
                                webDavSettings = settings
                                val count = repository.importBackupJson(webDavClient.download(settings))
                                restoreReminderSchedules(repository, scheduler)
                                count
                            }.onSuccess { count ->
                                backupStatus = "已从 WebDAV 恢复 $count 个事项"
                            }.onFailure { error ->
                                backupStatus = "恢复失败：${error.message.orEmpty()}"
                            }
                        }
                    }
                )
            }
        }
    }

    taskForNote?.let { task ->
        CompletionNoteDialog(
            task = task,
            onDismiss = { taskForNote = null },
            onSave = { note ->
                scope.launch {
                    repository.saveCompletionNote(task.id, note)
                    scheduler.cancel(task.id)
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
                    scheduler.cancel(task.id)
                    deleteCalendarEvent(context, task.calendarEventId)
                    repository.deleteTask(task.id)
                }
                taskForDelete = null
            }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AppHeader(
    tasks: List<TaskUiModel>,
    onOpenBackup: () -> Unit
) {
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val completedTasks = tasks.count { it.completed }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日"))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DianMao",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(9.dp)
                    ) {
                        Text(
                            text = "今日",
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Text(
                    text = "$today · 快速记录，稍后整理",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(
                onClick = onOpenBackup,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = "备份与恢复"
                )
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
        ) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(value = openTasks.toString(), label = "未完成", color = MaterialTheme.colorScheme.primary)
                SummaryPill(value = timedTasks.toString(), label = "有提醒", color = MaterialTheme.colorScheme.secondary)
                SummaryPill(value = completedTasks.toString(), label = "已复盘", color = TaskPriority.NotImportantNotUrgent.color)
            }
        }
    }
}

@Composable
private fun SummaryPill(value: String, label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.09f),
        contentColor = color,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun QuadrantDetailHeader(
    quadrant: TaskPriority,
    tasks: List<TaskUiModel>,
    onBack: () -> Unit
) {
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val completedTasks = tasks.count { it.completed }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.62f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回四象限"
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ColorDot(quadrant.color)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = quadrant.title,
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = quadrant.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(value = openTasks.toString(), label = "未完成", color = quadrant.color)
                SummaryPill(value = timedTasks.toString(), label = "有提醒", color = MaterialTheme.colorScheme.secondary)
                SummaryPill(value = completedTasks.toString(), label = "已复盘", color = TaskPriority.NotImportantNotUrgent.color)
            }
        }
    }
}

@Composable
private fun QuadrantDetailEmptyState(quadrant: TaskPriority) {
    Surface(
        color = quadrant.color.copy(alpha = 0.055f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "这个象限还没有事项",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "点右下角新增事项，会默认放入“${quadrant.title}”。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuadrantBoard(
    tasks: List<TaskUiModel>,
    modifier: Modifier = Modifier,
    onSelected: (TaskPriority) -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SectionHeader(title = "四象限", subtitle = "每个象限直接显示任务预览，点按象限查看完整列表")
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.70f))
        ) {
            Column {
                Row(modifier = Modifier.weight(1f)) {
                    QuadrantPane(
                        quadrant = TaskPriority.ImportantUrgent,
                        tasks = tasks.filter { it.quadrant == TaskPriority.ImportantUrgent },
                        modifier = Modifier.weight(1f),
                        onSelected = onSelected
                    )
                    BoardDivider(vertical = true)
                    QuadrantPane(
                        quadrant = TaskPriority.ImportantNotUrgent,
                        tasks = tasks.filter { it.quadrant == TaskPriority.ImportantNotUrgent },
                        modifier = Modifier.weight(1f),
                        onSelected = onSelected
                    )
                }
                BoardDivider(vertical = false)
                Row(modifier = Modifier.weight(1f)) {
                    QuadrantPane(
                        quadrant = TaskPriority.NotImportantUrgent,
                        tasks = tasks.filter { it.quadrant == TaskPriority.NotImportantUrgent },
                        modifier = Modifier.weight(1f),
                        onSelected = onSelected
                    )
                    BoardDivider(vertical = true)
                    QuadrantPane(
                        quadrant = TaskPriority.NotImportantNotUrgent,
                        tasks = tasks.filter { it.quadrant == TaskPriority.NotImportantNotUrgent },
                        modifier = Modifier.weight(1f),
                        onSelected = onSelected
                    )
                }
            }
        }
    }
}

@Composable
private fun BoardDivider(vertical: Boolean) {
    Box(
        modifier = if (vertical) {
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
        } else {
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.58f))
        }
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuadrantPane(
    quadrant: TaskPriority,
    tasks: List<TaskUiModel>,
    modifier: Modifier = Modifier,
    onSelected: (TaskPriority) -> Unit
) {
    val visibleTasks = tasks.take(1)
    val openCount = tasks.count { !it.completed }
    val paneColor by animateColorAsState(
        targetValue = quadrant.color.copy(alpha = 0.055f),
        animationSpec = tween(durationMillis = 180),
        label = "quadrantPaneColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(paneColor)
            .clickable { onSelected(quadrant) }
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(quadrant.color)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = quadrant.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = quadrant.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = quadrant.description,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = openCount.toString(),
                style = MaterialTheme.typography.titleLarge,
                color = quadrant.color
            )
        }
        if (visibleTasks.isEmpty()) {
            EmptyQuadrantHint(quadrant)
        } else {
            visibleTasks.forEach { task ->
                CompactQuadrantTask(task = task)
            }
            if (tasks.size > visibleTasks.size) {
                Text(
                    text = "还有 ${tasks.size - visibleTasks.size} 个，点按象限查看",
                    style = MaterialTheme.typography.labelMedium,
                    color = quadrant.color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun EmptyQuadrantHint(quadrant: TaskPriority) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.66f), RoundedCornerShape(11.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "暂无${quadrant.shortTitle}事项",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CompactQuadrantTask(
    task: TaskUiModel
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (task.completed) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.74f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                },
                RoundedCornerShape(10.dp)
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            CompletionMark(
                completed = task.completed,
                color = task.quadrant.color
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            task.dueLabel?.let {
                CompactMeta(icon = Icons.Rounded.Schedule, text = it)
            }
            if (task.useAlarm) {
                CompactMeta(icon = Icons.Rounded.Alarm, text = "响铃")
            }
            if (task.repeatRule != RepeatRule.None) {
                CompactMeta(icon = Icons.Rounded.Alarm, text = task.repeatRule.label)
            }
            if (task.useCalendar) {
                CompactMeta(icon = Icons.Rounded.CalendarMonth, text = "日历")
            }
            if (task.completionNote != null) {
                CompactMeta(icon = Icons.AutoMirrored.Rounded.Notes, text = "有心得")
            }
        }
    }
}

@Composable
private fun CompletionMark(completed: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .padding(top = 2.dp)
            .size(18.dp)
            .background(
                if (completed) color else Color.Transparent,
                CircleShape
            )
            .border(
                1.5.dp,
                if (completed) color else MaterialTheme.colorScheme.outline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
private fun CompactMeta(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskRow(
    task: TaskUiModel,
    onToggleComplete: (TaskUiModel) -> Unit,
    onEditTask: (TaskUiModel) -> Unit,
    onDeleteTask: (TaskUiModel) -> Unit,
    onEditNote: (TaskUiModel) -> Unit
) {
    var actionsExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (task.completed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "taskScale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Checkbox) { onToggleComplete(task) }
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = { onToggleComplete(task) },
                modifier = Modifier.semantics {
                    contentDescription = if (task.completed) "标记为未完成" else "标记为完成"
                }
            )
            Spacer(Modifier.width(8.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(task.quadrant.color)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = task.quadrant.title,
                        style = MaterialTheme.typography.labelLarge,
                        color = task.quadrant.color
                    )
                }
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    task.dueLabel?.let {
                        InfoChip(icon = Icons.Rounded.Schedule, text = it)
                    }
                    if (task.useCalendar) {
                        InfoChip(icon = Icons.Rounded.CalendarMonth, text = "系统日历")
                    }
                    if (task.useAlarm) {
                        InfoChip(icon = Icons.Rounded.Alarm, text = "响铃")
                    }
                    if (task.repeatRule != RepeatRule.None) {
                        InfoChip(icon = Icons.Rounded.Alarm, text = task.repeatRule.label)
                    }
                    if (task.completionNote != null) {
                        InfoChip(icon = Icons.AutoMirrored.Rounded.Notes, text = "有心得")
                    }
                }
                AnimatedContent(
                    targetState = task.completionNote,
                    label = "noteContent"
                ) { note ->
                    if (!note.isNullOrBlank()) {
                        Text(
                            text = "完成备注：$note",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(onClick = { onEditNote(task) }) {
                    Icon(
                        imageVector = if (task.completed) Icons.Rounded.EditNote else Icons.Rounded.DoneAll,
                        contentDescription = if (task.completed) "编辑完成备注" else "完成并写备注",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Box {
                    IconButton(onClick = { actionsExpanded = true }) {
                        Icon(
                            imageVector = Icons.Rounded.MoreVert,
                            contentDescription = "更多事项操作",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    DropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑事项") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Edit,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                actionsExpanded = false
                                onEditTask(task)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除事项", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            },
                            onClick = {
                                actionsExpanded = false
                                onDeleteTask(task)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    AssistChip(
        onClick = {},
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
        }
    )
}

@Composable
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(9.dp)
            .background(color, CircleShape)
            .border(1.dp, Color.White.copy(alpha = if (color.luminance() < 0.5f) 0.35f else 0.75f), CircleShape)
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddTaskPanel(
    visible: Boolean,
    editingTask: TaskUiModel?,
    initialPriority: TaskPriority,
    onDismiss: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onSave: (TaskFormState) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var priority by rememberSaveable(initialPriority) { mutableStateOf(initialPriority) }
    var dueDateMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var dueTimeMinutes by rememberSaveable { mutableStateOf<Int?>(null) }
    var useCalendar by rememberSaveable { mutableStateOf(false) }
    var useAlarm by rememberSaveable { mutableStateOf(false) }
    var repeatRule by rememberSaveable { mutableStateOf(RepeatRule.None) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val isEditing = editingTask != null

    LaunchedEffect(visible, editingTask?.id, initialPriority) {
        if (visible) {
            title = editingTask?.title.orEmpty()
            priority = editingTask?.quadrant ?: initialPriority
            dueDateMillis = editingTask?.dueAtMillis?.let { midnightMillis(it) }
            dueTimeMinutes = editingTask?.dueAtMillis?.let { minutesOfDay(it) }
            useCalendar = editingTask?.useCalendar ?: false
            useAlarm = editingTask?.useAlarm ?: false
            repeatRule = editingTask?.repeatRule ?: RepeatRule.None
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(if (isEditing) "编辑事项" else "新增事项", style = MaterialTheme.typography.titleLarge)
                            Text(
                                if (isEditing) "调整内容、象限、时间和提醒" else "先记录，再补象限和时间",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = if (isEditing) "关闭编辑面板" else "关闭新增面板"
                            )
                        }
                    }
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("事项内容") },
                        placeholder = { Text("例如：确认明天下午会议材料") },
                        singleLine = false,
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("放入象限", style = MaterialTheme.typography.titleMedium)
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TaskPriority.entries.forEach { item ->
                                FilterChip(
                                    selected = item == priority,
                                    onClick = { priority = item },
                                    label = { Text(item.shortTitle) },
                                    leadingIcon = { ColorDot(item.color) }
                                )
                            }
                        }
                    }
                    DateTimePickerRow(
                        dueDateMillis = dueDateMillis,
                        dueTimeMinutes = dueTimeMinutes,
                        onPickDate = {
                            showDatePicker(
                                context = context,
                                currentMillis = dueDateMillis,
                                onSelected = { selected ->
                                    dueDateMillis = selected
                                    if (dueTimeMinutes == null) dueTimeMinutes = 9 * 60
                                }
                            )
                        },
                        onPickTime = {
                            showTimePicker(
                                context = context,
                                currentMinutes = dueTimeMinutes,
                                onSelected = { dueTimeMinutes = it }
                            )
                        },
                        onClear = {
                            dueDateMillis = null
                            dueTimeMinutes = null
                            useCalendar = false
                            useAlarm = false
                            repeatRule = RepeatRule.None
                        }
                    )
                    ToggleRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "同步到系统日历",
                        subtitle = if (dueDateMillis == null) "先选择日期时间，再写入系统日历" else "保存后打开系统日历确认事件",
                        checked = useCalendar,
                        enabled = dueDateMillis != null,
                        onCheckedChange = {
                            useCalendar = it
                            if (it) onRequestCalendarPermission()
                        }
                    )
                    ToggleRow(
                        icon = Icons.Rounded.Alarm,
                        title = "设置响铃提醒",
                        subtitle = if (dueDateMillis == null) "先选择日期时间，再设置本机提醒" else "保存后写入 AlarmManager",
                        checked = useAlarm,
                        enabled = dueDateMillis != null,
                        onCheckedChange = {
                            useAlarm = it
                            if (!it) repeatRule = RepeatRule.None
                        }
                    )
                    RepeatRulePicker(
                        selected = repeatRule,
                        enabled = dueDateMillis != null && useAlarm,
                        onSelected = { repeatRule = it }
                    )
                    Button(
                        onClick = {
                            val trimmed = title.trim()
                            if (trimmed.isNotEmpty()) {
                                onSave(
                                    TaskFormState(
                                        title = trimmed,
                                        quadrant = priority.type,
                                        dueDateMillis = dueDateMillis,
                                        dueTimeMinutes = dueTimeMinutes,
                                        useCalendar = useCalendar,
                                        useAlarm = useAlarm,
                                        repeatRule = repeatRule
                                    )
                                )
                                if (!isEditing) {
                                    title = ""
                                    dueDateMillis = null
                                    dueTimeMinutes = null
                                    useCalendar = false
                                    useAlarm = false
                                    repeatRule = RepeatRule.None
                                    priority = initialPriority
                                }
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(if (isEditing) "保存修改" else "保存事项")
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupPanel(
    visible: Boolean,
    initialSettings: WebDavSettings,
    status: String?,
    onDismiss: () -> Unit,
    onLocalExport: () -> Unit,
    onLocalImport: () -> Unit,
    onSaveWebDav: (WebDavSettings) -> Unit,
    onUploadWebDav: (WebDavSettings) -> Unit,
    onRestoreWebDav: (WebDavSettings) -> Unit
) {
    var url by rememberSaveable { mutableStateOf(initialSettings.url) }
    var username by rememberSaveable { mutableStateOf(initialSettings.username) }
    var password by rememberSaveable { mutableStateOf(initialSettings.password) }
    val webDavSettings = WebDavSettings(url = url, username = username, password = password)
    val webDavReady = url.isNotBlank()

    LaunchedEffect(visible, initialSettings) {
        if (visible) {
            url = initialSettings.url
            username = initialSettings.username
            password = initialSettings.password
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("备份与恢复", style = MaterialTheme.typography.titleLarge)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "关闭备份面板")
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("本地备份", style = MaterialTheme.typography.titleMedium)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onLocalExport,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(Icons.Rounded.FileUpload, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("导出本地")
                            }
                            OutlinedButton(
                                onClick = onLocalImport,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(Icons.Rounded.FileDownload, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("本地恢复")
                            }
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("WebDAV", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(
                            value = url,
                            onValueChange = { url = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("备份文件 URL") },
                            placeholder = { Text("https:// 或局域网 http://192.168.x.x/backup.json") },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("用户名") },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.weight(1f),
                                label = { Text("密码") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { onSaveWebDav(webDavSettings) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(Icons.Rounded.Save, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("保存设置")
                            }
                            Button(
                                onClick = { onUploadWebDav(webDavSettings) },
                                enabled = webDavReady,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                Icon(Icons.Rounded.CloudUpload, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("上传备份")
                            }
                        }
                        OutlinedButton(
                            onClick = { onRestoreWebDav(webDavSettings) },
                            enabled = webDavReady,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            Icon(Icons.Rounded.CloudDownload, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("下载恢复")
                        }
                    }

                    if (!status.isNullOrBlank()) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = status,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RepeatRulePicker(
    selected: RepeatRule,
    enabled: Boolean,
    onSelected: (RepeatRule) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "重复提醒",
            style = MaterialTheme.typography.titleMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RepeatRule.entries.forEach { rule ->
                FilterChip(
                    selected = selected == rule,
                    onClick = { if (enabled) onSelected(rule) },
                    enabled = enabled || rule == RepeatRule.None,
                    label = { Text(rule.label) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DateTimePickerRow(
    dueDateMillis: Long?,
    dueTimeMinutes: Int?,
    onPickDate: () -> Unit,
    onPickTime: () -> Unit,
    onClear: () -> Unit
) {
    val label = if (dueDateMillis == null) {
        "未设置时间"
    } else {
        val date = Instant.ofEpochMilli(dueDateMillis)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .format(DateTimeFormatter.ofPattern("M月d日"))
        val time = dueTimeMinutes?.let {
            val hour = it / 60
            val minute = it % 60
            "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
        }
        listOfNotNull(date, time).joinToString(" ")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("时间", style = MaterialTheme.typography.titleMedium)
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onPickDate, shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (dueDateMillis == null) "选择日期" else "更改日期")
            }
            OutlinedButton(
                onClick = onPickTime,
                enabled = dueDateMillis != null,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (dueTimeMinutes == null) "选择时间" else "更改时间")
            }
            if (dueDateMillis != null) {
                TextButton(onClick = onClear) {
                    Text("清除")
                }
            }
        }
    }
}

private fun showDatePicker(
    context: android.content.Context,
    currentMillis: Long?,
    onSelected: (Long) -> Unit
) {
    val calendar = Calendar.getInstance().apply {
        if (currentMillis != null) timeInMillis = currentMillis
    }
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selected = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            onSelected(selected.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

private fun midnightMillis(timestamp: Long): Long {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

private fun minutesOfDay(timestamp: Long): Int {
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
    return time.hour * 60 + time.minute
}

private fun showTimePicker(
    context: android.content.Context,
    currentMinutes: Int?,
    onSelected: (Int) -> Unit
) {
    val calendar = Calendar.getInstance()
    val hour = currentMinutes?.div(60) ?: calendar.get(Calendar.HOUR_OF_DAY)
    val minute = currentMinutes?.rem(60) ?: calendar.get(Calendar.MINUTE)
    TimePickerDialog(
        context,
        { _, selectedHour, selectedMinute ->
            onSelected(selectedHour * 60 + selectedMinute)
        },
        hour,
        minute,
        true
    ).show()
}

@Composable
private fun DeleteTaskDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = { Text("删除事项") },
        text = {
            Text(
                text = "确定删除“${task.title}”？删除后不会保留提醒和完成备注。",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        Icon(icon, contentDescription = null, tint = if (enabled) MaterialTheme.colorScheme.secondary else contentColor)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = contentColor)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked && enabled,
            enabled = enabled,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun CompletionNoteDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by rememberSaveable(task.id) { mutableStateOf(task.completionNote.orEmpty()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text(if (task.completed) "编辑完成备注" else "完成并写备注") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("心得或复盘") },
                    placeholder = { Text("这件事完成后，我学到或确认了什么？") },
                    minLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(note) }) {
                Text("保存备注")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("稍后再写")
            }
        }
    )
}
