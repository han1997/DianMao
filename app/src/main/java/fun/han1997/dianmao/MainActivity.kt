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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import `fun`.han1997.dianmao.data.RepeatReminderCalculator
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskEntity
import `fun`.han1997.dianmao.data.TaskFormState
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.TaskCompletionEntity
import `fun`.han1997.dianmao.data.DateTimeFormatters
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

internal enum class InAppBackTarget {
    TaskDetail,
    BackupPanel,
    ArchivedPanel,
    TaskPanel,
    QuadrantDetail
}

internal enum class TimeFocusKind {
    Overdue,
    Today,
    Upcoming
}

internal data class TimeFocusItem(
    val task: TaskUiModel,
    val kind: TimeFocusKind,
    val dueAtMillis: Long,
    val dueLabel: String?
)

internal enum class HomeSmartView(val label: String) {
    Quadrants("四象限"),
    Today("今天"),
    Upcoming("近期"),
    Inbox("收集箱")
}

internal data class HomeSmartTaskItem(
    val task: TaskUiModel,
    val dueAtMillis: Long?,
    val dueLabel: String?
)

internal fun buildTimeFocusItems(
    tasks: List<TaskUiModel>,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault(),
    limit: Int = 3
): List<TimeFocusItem> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()

    return tasks.mapNotNull { task ->
        if (!task.isOpenTask()) return@mapNotNull null
        val dueAt = effectiveDueAtMillis(task, nowMillis, zoneId) ?: return@mapNotNull null
        val kind = when {
            dueAt < nowMillis -> TimeFocusKind.Overdue
            dueAt < tomorrowStartMillis -> TimeFocusKind.Today
            else -> TimeFocusKind.Upcoming
        }
        TimeFocusItem(
            task = task,
            kind = kind,
            dueAtMillis = dueAt,
            dueLabel = task.displayDueLabel(dueAt, zoneId)
        )
    }.sortedWith(
        compareBy<TimeFocusItem>(
            { it.kind.rank },
            { it.dueAtMillis }
        )
    ).take(limit)
}

internal fun buildHomeSmartTaskItems(
    tasks: List<TaskUiModel>,
    view: HomeSmartView,
    query: String,
    nowMillis: Long = System.currentTimeMillis(),
    zoneId: ZoneId = ZoneId.systemDefault()
): List<HomeSmartTaskItem> {
    val today = Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    val tomorrowStartMillis = today.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val upcomingEndMillis = today.plusDays(7).atStartOfDay(zoneId).toInstant().toEpochMilli()
    val normalizedQuery = query.trim()

    val indexedItems = tasks.mapIndexedNotNull { index, task ->
        if (!task.isOpenTask() || !task.matchesHomeSmartQuery(normalizedQuery)) {
            return@mapIndexedNotNull null
        }
        val dueAt = effectiveDueAtMillis(task, nowMillis, zoneId)
        val included = when (view) {
            HomeSmartView.Quadrants -> true
            HomeSmartView.Today -> dueAt != null && dueAt < tomorrowStartMillis
            HomeSmartView.Upcoming -> dueAt != null &&
                dueAt >= tomorrowStartMillis &&
                dueAt < upcomingEndMillis
            HomeSmartView.Inbox -> task.dueAtMillis == null
        }
        if (!included) {
            null
        } else {
            index to HomeSmartTaskItem(
                task = task,
                dueAtMillis = dueAt,
                dueLabel = dueAt?.let { task.displayDueLabel(it, zoneId) }
            )
        }
    }

    return when (view) {
        HomeSmartView.Today,
        HomeSmartView.Upcoming -> indexedItems.sortedWith(
            compareBy<Pair<Int, HomeSmartTaskItem>>(
                { it.second.dueAtMillis ?: Long.MAX_VALUE },
                { it.second.task.id }
            )
        )
        HomeSmartView.Quadrants,
        HomeSmartView.Inbox -> indexedItems.sortedBy { it.first }
    }.map { it.second }
}

private fun TaskUiModel.isOpenTask(): Boolean = !completed && !archived

private fun effectiveDueAtMillis(
    task: TaskUiModel,
    nowMillis: Long,
    zoneId: ZoneId
): Long? {
    val storedDueAt = task.dueAtMillis ?: return null
    return RepeatReminderCalculator.currentCycleDueAt(
        currentMillis = storedDueAt,
        repeatRule = task.repeatRule,
        nowMillis = nowMillis,
        zoneId = zoneId
    )
}

private fun TaskUiModel.displayDueLabel(dueAtMillis: Long, zoneId: ZoneId): String {
    val calculatedDueLabel = DateTimeFormatters.labelFor(
        dateMillis = midnightMillis(dueAtMillis, zoneId),
        minutesOfDay = minutesOfDay(dueAtMillis, zoneId)
    )
    return if (repeatRule.isCycleBased()) {
        calculatedDueLabel
    } else {
        dueLabel ?: calculatedDueLabel
    }
}

private fun TaskUiModel.matchesHomeSmartQuery(query: String): Boolean {
    if (query.isBlank()) return true
    return title.contains(query, ignoreCase = true) ||
        subTasks.any { it.title.contains(query, ignoreCase = true) }
}

private val TimeFocusKind.rank: Int
    get() = when (this) {
        TimeFocusKind.Overdue -> 0
        TimeFocusKind.Today -> 1
        TimeFocusKind.Upcoming -> 2
    }

internal fun resolveInAppBackTarget(
    showBackupPanel: Boolean,
    showArchivedPanel: Boolean,
    isTaskPanelVisible: Boolean,
    hasActivePriority: Boolean,
    taskForDetail: Boolean
): InAppBackTarget? {
    return when {
        showBackupPanel -> InAppBackTarget.BackupPanel
        showArchivedPanel -> InAppBackTarget.ArchivedPanel
        isTaskPanelVisible -> InAppBackTarget.TaskPanel
        taskForDetail -> InAppBackTarget.TaskDetail
        hasActivePriority -> InAppBackTarget.QuadrantDetail
        else -> null
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

private val TaskUiModel.completedSubTaskCount: Int
    get() = subTasks.count { it.completed }

private val TaskUiModel.subTaskProgressLabel: String
    get() = "${completedSubTaskCount}/${subTasks.size} 子事项"

private fun nextSubTaskId(items: List<TaskSubTask>): Long {
    return (items.maxOfOrNull { it.id } ?: 0L) + 1L
}

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

private fun openCalendarReminder(context: android.content.Context, task: TaskUiModel) {
    CalendarWriter.buildOpenReminderIntent(task)?.let { intent ->
        runCatching { context.startActivity(intent) }
    }
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
            subTasks = listOf(
                TaskSubTask(id = 1, title = "列出本周必须完成的事项"),
                TaskSubTask(id = 2, title = "标记今天要先推进的两项")
            ),
            completed = false,
            completionNote = null,
            archived = false,
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
            archived = false,
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
            archived = false,
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
            archived = false,
            createdAtMillis = now,
            updatedAtMillis = now
        )
    )
}
@Composable
private fun DianMaoTheme(content: @Composable () -> Unit) {
    val colorScheme = androidx.compose.material3.lightColorScheme(
        primary = Color(0xFFA84534),
        onPrimary = Color(0xFFFFFBFA),
        primaryContainer = Color(0xFFF5DED9),
        onPrimaryContainer = Color(0xFF3A100A),
        secondary = Color(0xFF2B6C9F),
        onSecondary = Color(0xFFF8FCFF),
        secondaryContainer = Color(0xFFDCEAF4),
        onSecondaryContainer = Color(0xFF071F31),
        background = Color(0xFFF8F8F5),
        onBackground = Color(0xFF1E211F),
        surface = Color(0xFFFFFCF9),
        onSurface = Color(0xFF1E211F),
        surfaceVariant = Color(0xFFEDEEEA),
        onSurfaceVariant = Color(0xFF5A625E),
        outline = Color(0xFFD9DDD8)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 27.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            titleLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            titleMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            bodyLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.1.sp
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.1.sp
            ),
            labelMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.05.sp
            ),
            labelLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp
            )
        ),
        content = content
    )
}

@Composable
private fun quietBorder(alpha: Float = 0.22f): BorderStroke {
    return BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = alpha))
}

@Composable
private fun quietTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
    focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
    disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
)

private val PanelShape = RoundedCornerShape(18.dp)
private val SheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
private val RowShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun DianMaoApp() {
    val context = LocalContext.current
    val repository = remember(context) { TaskRepository(context.applicationContext) }
    val scheduler = remember(context) { ReminderScheduler(context.applicationContext) }
    val backupSettingsStore = remember(context) { BackupSettingsStore(context.applicationContext) }
    val webDavClient = remember { WebDavBackupClient() }
    val scope = rememberCoroutineScope()
    val activeTasks by repository.activeTasks.collectAsState(initial = emptyList())
    val archivedTasks by repository.archivedTasks.collectAsState(initial = emptyList())

    LaunchedEffect(Unit) {
        repository.resetExpiredCycleTasks()
    }

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
    var backupStatus by rememberSaveable { mutableStateOf<String?>(null) }
    var homeSmartView by rememberSaveable { mutableStateOf(HomeSmartView.Quadrants) }
    var homeSearchQuery by rememberSaveable { mutableStateOf("") }
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
                    repository.toggleCompleted(target, false)
                }
            }
        } else {
            taskForNote = target
        }
    }

    val archiveTask: (TaskUiModel) -> Unit = { target ->
        scope.launch { repository.archiveTask(target.id) }
    }

    val unarchiveTask: (TaskUiModel) -> Unit = { target ->
        scope.launch { repository.unarchiveTask(target.id) }
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

    Surface(color = MaterialTheme.colorScheme.background) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            floatingActionButton = {
                if (taskForDetail == null) {
                    FloatingActionButton(
                        onClick = { showAddSheet = true },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "新增事项")
                    }
                }
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
                            .padding(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        AppHeader(
                            tasks = activeTasks,
                            onOpenBackup = {
                                backupStatus = null
                                showBackupPanel = true
                            },
                            onOpenArchived = {
                                showArchivedPanel = true
                            }
                        )
                        HomeSmartControls(
                            selectedView = homeSmartView,
                            query = homeSearchQuery,
                            resultCount = homeSmartItems.size,
                            onViewSelected = { homeSmartView = it },
                            onQueryChanged = { homeSearchQuery = it }
                        )
                        if (homeSmartView == HomeSmartView.Quadrants) {
                            TimeFocusLane(
                                tasks = homeVisibleTasks,
                                onTaskSelected = { taskForDetail = it }
                            )
                            QuadrantBoard(
                                tasks = homeVisibleTasks,
                                modifier = Modifier.weight(1f),
                                onSelected = { priority -> activePriority = priority }
                            )
                        } else {
                            HomeSmartTaskList(
                                smartItems = homeSmartItems,
                                selectedView = homeSmartView,
                                query = homeSearchQuery,
                                repository = repository,
                                onToggleComplete = toggleComplete,
                                onEditTask = { taskForEdit = it },
                                onDeleteTask = { taskForDelete = it },
                                onEditNote = { taskForNote = it },
                                onArchiveTask = archiveTask,
                                onViewDetail = { taskForDetail = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                } else {
                    val listState = rememberLazyListState()
                    val dragDropState = rememberDragDropListState(
                        lazyListState = listState,
                        onMove = { fromIndex, toIndex ->
                            val list = reorderableDetailTasks.toMutableList()
                            val item = list.removeAt(fromIndex)
                            list.add(toIndex, item)
                            reorderableDetailTasks = list
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
                            .pointerInput(Unit) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = { offset ->
                                        listState.layoutInfo.visibleItemsInfo
                                            .firstOrNull { item ->
                                                offset.y.toInt() in item.offset..(item.offset + item.size)
                                            }
                                            ?.let {
                                                // Subtract 1 to account for header item at index 0
                                                val taskIndex = it.index - 1
                                                if (taskIndex >= 0) {
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
                        contentPadding = PaddingValues(start = 18.dp, top = 6.dp, end = 18.dp, bottom = 112.dp),
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
                            if (reorderableDetailTasks.isEmpty()) {
                                item {
                                    QuadrantDetailEmptyState(priority)
                                }
                            } else {
                                itemsIndexed(reorderableDetailTasks, key = { _, task -> task.id }) { index, task ->
                                    val isBeingDragged = dragDropState.draggedIndex == index
                                    val offsetY = if (isBeingDragged) dragDropState.draggedOffset else 0f

                                    Box(
                                        modifier = Modifier
                                            .zIndex(if (isBeingDragged) 1f else 0f)
                                            .graphicsLayer {
                                                translationY = offsetY
                                                shadowElevation = if (isBeingDragged) 8.dp.toPx() else 0f
                                                scaleX = if (isBeingDragged) 1.02f else 1f
                                                scaleY = if (isBeingDragged) 1.02f else 1f
                                            }
                                            .animateItemPlacement()
                                    ) {
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
                                repository.updateSubTaskCompleted(task.id, subTaskId, completed)
                            }
                        },
                        onDeleteCompletion = { completionId ->
                            scope.launch {
                                repository.deleteCompletion(completionId)
                            }
                        },
                        onUpdateCompletionNote = { completionId, note ->
                            scope.launch {
                                repository.updateCompletionNote(completionId, note)
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
                        scope.launch {
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
                        if (previousTask != null) {
                            dismissTaskPanel()
                        }
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
                                repository.deleteCompletion(completionId)
                            }
                        },
                        onUpdateCompletionNote = { completionId, note ->
                            scope.launch {
                                repository.updateCompletionNote(completionId, note)
                            }
                        }
                    )
                }

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
                ArchivedPanel(
                    visible = showArchivedPanel,
                    tasks = archivedTasks,
                    onDismiss = { showArchivedPanel = false },
                    onUnarchive = { task ->
                        scope.launch {
                            repository.unarchiveTask(task.id)
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
                    val updated = repository.saveCompletionNote(task.id, note)
                    scheduler.cancel(task.id)
                    if (updated != null && updated.useAlarm && updated.dueAtMillis != null) {
                        val uiTask = task.copy(
                            dueAtMillis = updated.dueAtMillis,
                            dueLabel = updated.dueLabel
                        )
                        scheduler.schedule(uiTask)
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
                    scheduler.cancel(task.id)
                    deleteCalendarEvent(context, task.calendarEventId)
                    repository.deleteTask(task.id)
                }
                taskForDelete = null
            }
        )
    }

    taskForUncompletion?.let { task ->
        UncompleteRecurringTaskDialog(
            task = task,
            onDismiss = { taskForUncompletion = null },
            onConfirm = {
                scope.launch {
                    repository.toggleCompleted(task, false)
                }
                taskForUncompletion = null
            }
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun AppHeader(
    tasks: List<TaskUiModel>,
    onOpenBackup: () -> Unit,
    onOpenArchived: () -> Unit
) {
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val completedTasks = tasks.count { it.completed }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日"))

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = ChipShape
                    ) {
                        Text(
                            text = "今日",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.1.sp
                            )
                        )
                    }
                }
                Text(
                    text = "$today · 快速记录，稍后整理",
                    style = MaterialTheme.typography.bodySmall.copy(
                        letterSpacing = 0.05.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                onClick = onOpenArchived,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                border = quietBorder(0.16f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Archive,
                        contentDescription = "归档事项",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Surface(
                onClick = onOpenBackup,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                border = quietBorder(0.16f),
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = "备份与恢复",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RowShape,
            border = quietBorder(0.16f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SummaryPill(value = openTasks.toString(), label = "未完成", color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                SummaryPill(value = timedTasks.toString(), label = "有提醒", color = MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
                SummaryPill(value = completedTasks.toString(), label = "已复盘", color = TaskPriority.NotImportantNotUrgent.color, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun SummaryPill(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = color.copy(alpha = 0.09f),
        contentColor = color,
        shape = ChipShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 9.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = color.copy(alpha = 0.78f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun HomeSmartControls(
    selectedView: HomeSmartView,
    query: String,
    resultCount: Int,
    onViewSelected: (HomeSmartView) -> Unit,
    onQueryChanged: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RowShape,
        border = quietBorder(0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "视图",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$resultCount 项",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.76f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HomeSmartView.entries.forEach { view ->
                    FilterChip(
                        selected = selectedView == view,
                        onClick = { onViewSelected(view) },
                        label = {
                            Text(
                                text = view.label,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    )
                }
            }
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                placeholder = {
                    Text(
                        text = "搜索当前视图",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                colors = quietTextFieldColors(),
                shape = RowShape
            )
        }
    }
}

@Composable
private fun TimeFocusLane(
    tasks: List<TaskUiModel>,
    onTaskSelected: (TaskUiModel) -> Unit
) {
    val focusItems = buildTimeFocusItems(tasks)
    if (focusItems.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RowShape,
        border = quietBorder(0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "今日关注",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${focusItems.size} 项",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                focusItems.forEach { item ->
                    TimeFocusRow(
                        item = item,
                        onClick = { onTaskSelected(item.task) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TimeFocusRow(
    item: TimeFocusItem,
    onClick: () -> Unit
) {
    val color = when (item.kind) {
        TimeFocusKind.Overdue -> MaterialTheme.colorScheme.error
        TimeFocusKind.Today -> MaterialTheme.colorScheme.primary
        TimeFocusKind.Upcoming -> MaterialTheme.colorScheme.secondary
    }
    val label = when (item.kind) {
        TimeFocusKind.Overdue -> "过期"
        TimeFocusKind.Today -> "今天"
        TimeFocusKind.Upcoming -> "近期"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(color.copy(alpha = 0.07f), RowShape)
            .border(1.dp, color.copy(alpha = 0.12f), RowShape)
            .clickable(onClick = onClick, role = Role.Button)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.12f),
            contentColor = color,
            shape = ChipShape
        ) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                maxLines = 1
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = item.task.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item.dueLabel?.let {
                    CompactMeta(icon = Icons.Rounded.Schedule, text = it)
                }
                CompactMeta(icon = Icons.Rounded.Inventory2, text = item.task.quadrant.shortTitle)
            }
        }
    }
}

@Composable
private fun HomeSmartTaskList(
    smartItems: List<HomeSmartTaskItem>,
    selectedView: HomeSmartView,
    query: String,
    repository: TaskRepository,
    onToggleComplete: (TaskUiModel) -> Unit,
    onEditTask: (TaskUiModel) -> Unit,
    onDeleteTask: (TaskUiModel) -> Unit,
    onEditNote: (TaskUiModel) -> Unit,
    onArchiveTask: (TaskUiModel) -> Unit,
    onViewDetail: (TaskUiModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        if (smartItems.isEmpty()) {
            item {
                HomeSmartEmptyState(
                    selectedView = selectedView,
                    query = query
                )
            }
        } else {
            items(
                items = smartItems,
                key = { it.task.id }
            ) { smartItem ->
                TaskRow(
                    task = smartItem.task,
                    repository = repository,
                    onToggleComplete = onToggleComplete,
                    onEditTask = onEditTask,
                    onDeleteTask = onDeleteTask,
                    onEditNote = onEditNote,
                    onArchiveTask = onArchiveTask,
                    onViewDetail = onViewDetail,
                    showQuadrantMeta = true,
                    dueLabelOverride = smartItem.dueLabel
                )
            }
        }
    }
}

@Composable
private fun HomeSmartEmptyState(
    selectedView: HomeSmartView,
    query: String
) {
    val title = if (query.isBlank()) {
        when (selectedView) {
            HomeSmartView.Today -> "今天没有待处理事项"
            HomeSmartView.Upcoming -> "未来 7 天没有已安排事项"
            HomeSmartView.Inbox -> "收集箱是空的"
            HomeSmartView.Quadrants -> "没有待处理事项"
        }
    } else {
        "没有匹配事项"
    }
    val subtitle = if (query.isBlank()) {
        when (selectedView) {
            HomeSmartView.Today -> "过期和今天的定时事项会显示在这里。"
            HomeSmartView.Upcoming -> "明天起 7 天内的定时事项会显示在这里。"
            HomeSmartView.Inbox -> "未设置时间的事项会显示在这里，方便稍后规划。"
            HomeSmartView.Quadrants -> "点击右下角新增事项，先记录，再整理。"
        }
    } else {
        "换个关键词，或回到四象限继续浏览。"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RowShape,
        border = quietBorder(0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuadrantDetailHeader(
    quadrant: TaskPriority,
    tasks: List<TaskUiModel>,
    onBack: () -> Unit
) {
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val completedTasks = tasks.count { it.completed }

    Surface(
        color = quadrant.color.copy(alpha = 0.08f),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Accent strip
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(quadrant.color, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
            // Title row with inline back
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回四象限",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                ColorDot(quadrant.color)
                Text(
                    modifier = Modifier.weight(1f),
                    text = quadrant.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Description
            Text(
                text = quadrant.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            // Inline stats
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                InlineStatDot(count = openTasks, label = "未完成", color = quadrant.color)
                InlineStatDot(count = timedTasks, label = "有提醒", color = MaterialTheme.colorScheme.secondary)
                InlineStatDot(count = completedTasks, label = "已复盘", color = TaskPriority.NotImportantNotUrgent.color)
            }
        }
    }
}

@Composable
private fun InlineStatDot(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            color = color.copy(alpha = 0.78f)
        )
    }
}

@Composable
private fun QuadrantDetailEmptyState(quadrant: TaskPriority) {
    Surface(
        color = quadrant.color.copy(alpha = 0.07f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Accent strip matching header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(quadrant.color, RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ColorDot(quadrant.color)
                Text(
                    text = "这个象限还没有事项",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
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
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = PanelShape,
        border = quietBorder(0.14f),
        shadowElevation = 0.dp
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

@Composable
private fun BoardDivider(vertical: Boolean) {
    Box(
        modifier = if (vertical) {
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
        } else {
            Modifier
                .height(1.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
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
    val visibleTasks = tasks.take(5)
    val openCount = tasks.count { !it.completed }
    val paneColor by animateColorAsState(
        targetValue = quadrant.color.copy(alpha = 0.045f),
        animationSpec = tween(durationMillis = 200),
        label = "quadrantPaneColor"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(paneColor, shape = RoundedCornerShape(18.dp))
            .clickable { onSelected(quadrant) }
            .verticalScroll(rememberScrollState())
            .padding(9.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ColorDot(quadrant.color)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = quadrant.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.1.sp
                        ),
                        color = quadrant.color,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = quadrant.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.02.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = openCount.toString(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                ),
                color = quadrant.color.copy(alpha = 0.95f)
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
                    text = "+${tasks.size - visibleTasks.size}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = quadrant.color.copy(alpha = 0.72f),
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f), ChipShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "暂无${quadrant.shortTitle}事项",
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.05.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun CompactQuadrantTask(
    task: TaskUiModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (task.completed) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f)
                } else {
                    MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                },
                ChipShape
            )
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.10f), ChipShape)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        CompletionMark(
            completed = task.completed,
            color = task.quadrant.color
        )
        Text(
            text = task.title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
            ),
            color = if (task.completed)
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            else
                MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        if (task.dueLabel != null || task.useAlarm || task.useCalendar) {
            Icon(
                imageVector = Icons.Rounded.Schedule,
                contentDescription = "有提醒",
                modifier = Modifier.size(14.dp),
                tint = task.quadrant.color.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun CompletionMark(completed: Boolean, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
                tint = MaterialTheme.colorScheme.onPrimary,
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
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f), ChipShape)
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.74f),
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
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
    repository: TaskRepository,
    onToggleComplete: (TaskUiModel) -> Unit,
    onEditTask: (TaskUiModel) -> Unit,
    onDeleteTask: (TaskUiModel) -> Unit,
    onEditNote: (TaskUiModel) -> Unit,
    onViewDetail: (TaskUiModel) -> Unit = {},
    onArchiveTask: (TaskUiModel) -> Unit = {},
    showQuadrantMeta: Boolean = true,
    dueLabelOverride: String? = null,
    modifier: Modifier = Modifier
) {
    var canComplete by remember { mutableStateOf(true) }
    var actionsExpanded by remember { mutableStateOf(false) }
    val displayedDueLabel = dueLabelOverride ?: task.dueLabel
    val scale by animateFloatAsState(
        targetValue = if (task.completed) 0.985f else 1f,
        animationSpec = tween(durationMillis = 180),
        label = "taskScale"
    )

    LaunchedEffect(task.id, task.lastCompletedAtMillis, task.completed, task.repeatRule) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RowShape,
        border = quietBorder(if (task.completed) 0.12f else 0.18f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            CompletionMark(
                completed = task.completed,
                color = task.quadrant.color,
                modifier = Modifier
                    .clickable(
                        enabled = canComplete || task.completed,
                        role = Role.Checkbox
                    ) { onToggleComplete(task) }
                    .semantics {
                        contentDescription = if (task.completed) "标记为未完成" else "标记为完成"
                    }
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onViewDetail(task) }
                    .semantics {
                        contentDescription = "查看事项详情：${task.title}"
                    },
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 0.sp
                    ),
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (task.subTasks.isNotEmpty()) {
                    SubTaskProgressSummary(
                        completed = task.completedSubTaskCount,
                        total = task.subTasks.size,
                        color = task.quadrant.color
                    )
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (showQuadrantMeta) {
                        CompactMeta(icon = Icons.Rounded.Inventory2, text = task.quadrant.shortTitle)
                    }
                    displayedDueLabel?.let {
                        CompactMeta(icon = Icons.Rounded.Schedule, text = it)
                    }
                    if (task.useCalendar) {
                        CompactMeta(icon = Icons.Rounded.CalendarMonth, text = "系统日历")
                    }
                    if (task.useAlarm) {
                        CompactMeta(icon = Icons.Rounded.Alarm, text = "响铃")
                    }
                    if (task.repeatRule != RepeatRule.None) {
                        CompactMeta(icon = Icons.Rounded.Alarm, text = task.repeatRule.label)
                    }
                    if (task.subTasks.isNotEmpty()) {
                        CompactMeta(icon = Icons.Rounded.CheckCircle, text = task.subTaskProgressLabel)
                    }
                    if (task.completionNote != null) {
                        CompactMeta(icon = Icons.AutoMirrored.Rounded.Notes, text = "有心得")
                    }
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = { onEditNote(task) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (task.completed) Icons.Rounded.EditNote else Icons.Rounded.DoneAll,
                        contentDescription = if (task.completed) "编辑完成备注" else "完成并写备注",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Box {
                    IconButton(
                        onClick = { actionsExpanded = true },
                        modifier = Modifier.size(48.dp)
                    ) {
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
                        if (task.completed) {
                            DropdownMenuItem(
                                text = { Text("归档") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Rounded.Archive,
                                        contentDescription = null
                                    )
                                },
                                onClick = {
                                    actionsExpanded = false
                                    onArchiveTask(task)
                                }
                            )
                        }
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
private fun ColorDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(color, CircleShape)
            .border(1.2.dp, MaterialTheme.colorScheme.surface.copy(alpha = if (color.luminance() < 0.5f) 0.38f else 0.72f), CircleShape)
    )
}

@Composable
private fun TaskFormSection(
    title: String,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = PanelShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
        border = quietBorder(0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}

@Composable
private fun FormSettingBlock(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.26f else 0.16f),
        border = quietBorder(if (enabled) 0.10f else 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.82f else 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            content()
        }
    }
}

@Composable
private fun SubTaskProgressSummary(
    completed: Int,
    total: Int,
    color: Color
) {
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "子事项",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$completed/$total",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(999.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(color.copy(alpha = 0.82f), RoundedCornerShape(999.dp))
            )
        }
    }
}

@Composable
private fun SubTaskEditor(
    subTasks: List<TaskSubTask>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onTitleChange: (Long, String) -> Unit,
    onCheckedChange: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    val completed = subTasks.count { it.completed }
    TaskFormSection(
        title = "子事项",
        subtitle = if (subTasks.isEmpty()) "可选" else "$completed/${subTasks.size} 已完成"
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = { Text("添加一个小任务") },
                singleLine = true,
                shape = RowShape,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                colors = quietTextFieldColors()
            )
            FilledTonalButton(
                onClick = onAdd,
                enabled = draft.isNotBlank(),
                modifier = Modifier.heightIn(min = 56.dp),
                shape = RowShape,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("添加")
            }
        }

        if (subTasks.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RowShape,
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                border = quietBorder(0.10f)
            ) {
                Text(
                    text = "暂无子事项",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                subTasks.forEach { item ->
                    SubTaskEditRow(
                        item = item,
                        onTitleChange = { onTitleChange(item.id, it) },
                        onCheckedChange = { onCheckedChange(item.id, it) },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubTaskEditRow(
    item: TaskSubTask,
    onTitleChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = quietBorder(0.10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = onCheckedChange
            )
            OutlinedTextField(
                value = item.title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                placeholder = { Text("子事项") },
                singleLine = true,
                shape = RowShape,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = quietTextFieldColors()
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "删除子事项",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
private fun SubTaskReadOnlySection(
    task: TaskUiModel,
    onToggleSubTask: (Long, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "子事项",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = task.subTaskProgressLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RowShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
            border = quietBorder(0.10f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                task.subTasks.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = item.completed,
                                role = Role.Checkbox,
                                onValueChange = { checked -> onToggleSubTask(item.id, checked) }
                            )
                            .heightIn(min = 48.dp)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = item.completed,
                            onCheckedChange = null
                        )
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.completed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
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
    var subTasks by remember { mutableStateOf<List<TaskSubTask>>(emptyList()) }
    var subTaskDraft by rememberSaveable { mutableStateOf("") }
    var saveFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val formScrollState = rememberScrollState()
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
            subTasks = editingTask?.subTasks ?: emptyList()
            subTaskDraft = ""
            saveFeedback = null
        }
    }

    LaunchedEffect(saveFeedback) {
        if (saveFeedback != null) {
            kotlinx.coroutines.delay(2200)
            saveFeedback = null
        }
    }

    fun addSubTask() {
        val trimmed = subTaskDraft.trim()
        if (trimmed.isBlank()) return
        subTasks = subTasks + TaskSubTask(
            id = nextSubTaskId(subTasks),
            title = trimmed,
            completed = false
        )
        subTaskDraft = ""
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF211815).copy(alpha = 0.16f))
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
                    .fillMaxHeight(0.90f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = SheetShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f), CircleShape)
                    )
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(formScrollState),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        TaskFormSection(
                            title = "事项",
                            subtitle = if (isEditing) "调整标题和拆分结构" else "先记录要推进的事"
                        ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("事项内容") },
                            placeholder = { Text("例如：确认明天下午会议材料") },
                            singleLine = false,
                            minLines = 2,
                            maxLines = 4,
                            shape = RowShape,
                            textStyle = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 17.sp
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            colors = quietTextFieldColors()
                        )
                        }
                        SubTaskEditor(
                            subTasks = subTasks,
                            draft = subTaskDraft,
                            onDraftChange = { subTaskDraft = it },
                            onAdd = { addSubTask() },
                            onTitleChange = { id, newTitle ->
                                subTasks = subTasks.map { item ->
                                    if (item.id == id) item.copy(title = newTitle) else item
                                }
                            },
                            onCheckedChange = { id, checked ->
                                subTasks = subTasks.map { item ->
                                    if (item.id == id) item.copy(completed = checked) else item
                                }
                            },
                            onDelete = { id ->
                                subTasks = subTasks.filterNot { it.id == id }
                            }
                        )
                        FormSettingBlock(title = "象限") {
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(7.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
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
                            }
                        )
                        ToggleRow(
                            icon = Icons.Rounded.CalendarMonth,
                            title = "系统日历",
                            subtitle = if (dueDateMillis == null) "选择时间后可同步" else "保存后写入系统日历",
                            checked = useCalendar,
                            enabled = dueDateMillis != null,
                            onCheckedChange = {
                                useCalendar = it
                                if (it) onRequestCalendarPermission()
                            }
                        )
                        ToggleRow(
                            icon = Icons.Rounded.Alarm,
                            title = "响铃提醒",
                            subtitle = if (dueDateMillis == null) "选择时间后可提醒" else "保存后写入 AlarmManager",
                            checked = useAlarm,
                            enabled = dueDateMillis != null,
                            onCheckedChange = {
                                useAlarm = it
                                if (!it) repeatRule = RepeatRule.None
                            }
                        )
                        RepeatRulePicker(
                            selected = repeatRule,
                            enabled = true,
                            onSelected = { repeatRule = it }
                        )
                    }
                    AnimatedVisibility(
                        visible = saveFeedback != null,
                        enter = fadeIn(tween(120)) + slideInVertically(tween(160)) { it / 3 },
                        exit = fadeOut(tween(120))
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RowShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f),
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            border = quietBorder(0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = saveFeedback.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
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
                                        repeatRule = repeatRule,
                                        subTasks = subTasks
                                    )
                                )
                                if (!isEditing) {
                                    saveFeedback = "已保存到 ${priority.shortTitle}，可继续记录"
                                    title = ""
                                    dueDateMillis = null
                                    dueTimeMinutes = null
                                    useCalendar = false
                                    useAlarm = false
                                    repeatRule = RepeatRule.None
                                    subTasks = emptyList()
                                    subTaskDraft = ""
                                }
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RowShape,
                        contentPadding = PaddingValues(vertical = 13.dp),
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
                .background(Color(0xFF211815).copy(alpha = 0.16f))
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
                shape = SheetShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
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

@Composable
private fun ArchivedPanel(
    visible: Boolean,
    tasks: List<TaskUiModel>,
    onDismiss: () -> Unit,
    onUnarchive: (TaskUiModel) -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF211815).copy(alpha = 0.16f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.75f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shape = PanelShape
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "归档事项",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Rounded.Close,
                                contentDescription = "关闭",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    if (tasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.Inventory2,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Text(
                                    text = "暂无归档事项",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                ArchivedTaskItem(
                                    task = task,
                                    onUnarchive = { onUnarchive(task) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedTaskItem(
    task: TaskUiModel,
    onUnarchive: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        shape = RowShape,
        border = quietBorder(0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                if (task.completionNote != null) {
                    Text(
                        text = task.completionNote,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            IconButton(onClick = onUnarchive) {
                Icon(
                    Icons.Rounded.Unarchive,
                    contentDescription = "取消归档",
                    tint = MaterialTheme.colorScheme.primary
                )
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
    FormSettingBlock(
        title = "重复规则",
        enabled = enabled
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            RepeatRule.entries.forEach { rule ->
                FilterChip(
                    selected = selected == rule,
                    onClick = { if (enabled) onSelected(rule) },
                    enabled = enabled,
                    shape = ChipShape,
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

    FormSettingBlock(
        title = "时间",
        subtitle = label
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(7.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = onPickDate,
                shape = RowShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.CalendarMonth, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text(if (dueDateMillis == null) "选择日期" else "更改日期")
            }
            OutlinedButton(
                onClick = onPickTime,
                enabled = dueDateMillis != null,
                shape = RowShape,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
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

private fun midnightMillis(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Long {
    return Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
        .toLocalDate()
        .atStartOfDay(zoneId)
        .toInstant()
        .toEpochMilli()
}

private fun minutesOfDay(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Int {
    val time = Instant.ofEpochMilli(timestamp)
        .atZone(zoneId)
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
private fun UncompleteRecurringTaskDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Alarm,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("取消完成重复事项？") },
        text = {
            Text(
                text = "这是一个重复事项，取消完成后将重新进入待办状态。是否继续？",
                style = MaterialTheme.typography.bodyLarge
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("确认取消完成")
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
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) },
        shape = RowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.26f else 0.16f),
        border = quietBorder(if (enabled) 0.10f else 0.06f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val contentColor = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (enabled) MaterialTheme.colorScheme.secondary else contentColor.copy(alpha = 0.62f)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = contentColor)
                Text(
                    subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.82f else 0.58f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Switch(
                checked = checked && enabled,
                enabled = enabled,
                onCheckedChange = onCheckedChange
            )
        }
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
                    minLines = 3,
                    maxLines = 6,
                    shape = RowShape,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    colors = quietTextFieldColors()
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

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropListState {
    val state = remember { DragDropListState(lazyListState, onMove) }
    return state
}

class DragDropListState(
    private val lazyListState: LazyListState,
    private val onMove: (Int, Int) -> Unit
) {
    var draggedIndex by mutableStateOf<Int?>(null)
    var draggedOffset by mutableStateOf(0f)

    fun onDragStart(index: Int) {
        draggedIndex = index
    }

    fun onDrag(delta: Float) {
        draggedOffset += delta

        val draggedIdx = draggedIndex ?: return
        val startOffset = draggedOffset
        
        val currentElement = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull { it.index == draggedIdx }
        val targetElement = lazyListState.layoutInfo.visibleItemsInfo.find {
            val targetCenter = it.offset + it.size / 2
            val draggedCenter = (currentElement?.offset ?: 0) + startOffset + (currentElement?.size ?: 0) / 2

            if (startOffset > 0) {
                draggedCenter > targetCenter && it.index > draggedIdx
            } else {
                draggedCenter < targetCenter && it.index < draggedIdx
            }
        }

        if (targetElement != null) {
            onMove(draggedIdx, targetElement.index)
            draggedIndex = targetElement.index
            draggedOffset = 0f
        }
    }

    fun onDragEnd() {
        draggedIndex = null
        draggedOffset = 0f
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringTaskHistoryPanel(
    task: TaskUiModel,
    repository: TaskRepository,
    onDismiss: () -> Unit,
    onEdit: (TaskUiModel) -> Unit,
    onComplete: (TaskUiModel) -> Unit,
    onDeleteCompletion: (Long) -> Unit,
    onUpdateCompletionNote: (Long, String?) -> Unit = { _, _ -> }
) {
    val completions by repository.observeCompletions(task.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var canComplete by remember { mutableStateOf(false) }

    LaunchedEffect(task) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onEdit(task) }) {
                    Icon(Icons.Rounded.Edit, contentDescription = "编辑")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Task info
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = {},
                    label = { Text(task.quadrant.label) },
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(
                                    Color(task.quadrant.colorInt()),
                                    shape = CircleShape
                                )
                        )
                    }
                )
                Text(
                    text = task.repeatRule.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${task.completionCount}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "总完成次数",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${completions.size}",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "历史记录",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Complete button
            Button(
                onClick = { onComplete(task) },
                enabled = canComplete,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.DoneAll, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (canComplete) "完成本次" else "本周期已完成")
            }

            Spacer(modifier = Modifier.height(24.dp))

            // History section
            Text(
                text = "完成历史",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (completions.isEmpty()) {
                Text(
                    text = "暂无完成记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 32.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(completions, key = { it.id }) { completion ->
                        CompletionItem(
                            completion = completion,
                            onDelete = { onDeleteCompletion(completion.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun CompletionItem(
    completion: TaskCompletionEntity,
    onDelete: () -> Unit,
    onUpdateNote: (Long, String?) -> Unit = { _, _ -> }
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isEditingNote by remember { mutableStateOf(false) }
    var editNoteText by remember { mutableStateOf(completion.note.orEmpty()) }
    val focusManager = LocalFocusManager.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateTimeFormatters.formatTimestamp(completion.completedAtMillis),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            editNoteText = completion.note.orEmpty()
                            isEditingNote = true
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = "编辑备注",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            if (isEditingNote) {
                OutlinedTextField(
                    value = editNoteText,
                    onValueChange = { editNoteText = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    placeholder = { Text("写点什么") },
                    textStyle = MaterialTheme.typography.bodyMedium,
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    shape = RowShape,
                    colors = quietTextFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        onUpdateNote(completion.id, editNoteText)
                        isEditingNote = false
                        focusManager.clearFocus()
                    })
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = {
                            isEditingNote = false
                            editNoteText = completion.note.orEmpty()
                        }
                    ) { Text("取消") }
                    TextButton(
                        onClick = {
                            onUpdateNote(completion.id, editNoteText)
                            isEditingNote = false
                        }
                    ) { Text("保存") }
                }
            } else if (!completion.note.isNullOrBlank()) {
                Text(
                    text = completion.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除完成记录") },
            text = { Text("确定要删除这条完成记录吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TaskDetailScreen(
    task: TaskUiModel,
    repository: TaskRepository,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onToggleComplete: (TaskUiModel) -> Unit,
    onDelete: (TaskUiModel) -> Unit,
    onEditNote: (TaskUiModel) -> Unit,
    onArchive: (TaskUiModel) -> Unit,
    onOpenCalendarReminder: (TaskUiModel) -> Unit,
    onToggleSubTask: (Long, Boolean) -> Unit,
    onDeleteCompletion: (Long) -> Unit,
    onUpdateCompletionNote: (Long, String?) -> Unit
) {
    val completions by repository.observeCompletions(task.id).collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f))
                    .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "事项详情",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "编辑",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 18.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Task title
                item {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                    )
                }

                // Meta section
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(task.quadrant.color, CircleShape)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = task.quadrant.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = task.quadrant.color,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            task.dueLabel?.let {
                                CompactMeta(icon = Icons.Rounded.Schedule, text = it)
                            }
                            if (task.useCalendar) {
                                CompactMeta(icon = Icons.Rounded.CalendarMonth, text = "系统日历")
                            }
                            if (task.useAlarm) {
                                CompactMeta(icon = Icons.Rounded.Alarm, text = "响铃")
                            }
                            if (task.repeatRule != RepeatRule.None) {
                                CompactMeta(icon = Icons.Rounded.Repeat, text = task.repeatRule.label)
                            }
                            if (task.completionCount > 0) {
                                CompactMeta(icon = Icons.Rounded.CheckCircle, text = "已完成 ${task.completionCount} 次")
                            }
                        }
                    }
                }

                if (task.subTasks.isNotEmpty()) {
                    item {
                        SubTaskReadOnlySection(
                            task = task,
                            onToggleSubTask = onToggleSubTask
                        )
                    }
                }

                if (task.useCalendar && task.dueAtMillis != null) {
                    item {
                        FilledTonalButton(
                            onClick = { onOpenCalendarReminder(task) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(
                                Icons.Rounded.CalendarMonth,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("打开系统日历提醒")
                        }
                    }
                }

                // Completion note (if exists)
                task.completionNote?.takeIf { it.isNotBlank() }?.let { note ->
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "完成心得",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Surface(
                                shape = RowShape,
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                                border = quietBorder(0.12f)
                            ) {
                                Text(
                                    text = note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(14.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Stats row
                if (task.repeatRule != RepeatRule.None || completions.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "${task.completionCount}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = task.quadrant.color
                                )
                                Text(
                                    text = "总完成次数",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column {
                                Text(
                                    text = "${completions.size}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "历史记录",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            task.lastCompletedAtMillis?.let {
                                Column {
                                    Text(
                                        text = DateTimeFormatters.formatTimestamp(it),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "上次完成",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // Completion history
                item {
                    Text(
                        text = "完成历史",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                if (completions.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier.padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Rounded.History,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (task.repeatRule != RepeatRule.None) "完成循环事项后这里会记录每次完成" else "暂无完成记录",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(completions, key = { it.id }) { completion ->
                        CompletionItem(
                            completion = completion,
                            onDelete = { onDeleteCompletion(completion.id) },
                            onUpdateNote = { id, note -> onUpdateCompletionNote(id, note) }
                        )
                    }
                }
            }

            // Bottom action bar
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = { onToggleComplete(task) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (task.completed) Icons.Rounded.Replay else Icons.Rounded.DoneAll,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (task.completed) "标为未完成" else "完成")
                    }
                    if (task.completed) {
                        FilledTonalButton(
                            onClick = { onArchive(task) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Rounded.Archive,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("归档")
                        }
                    }
                    IconButton(
                        onClick = { onDelete(task) },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            Icons.Rounded.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
