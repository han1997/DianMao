package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskFormState
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.normalizedTimeSettings
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.SubTaskEditor
import `fun`.han1997.dianmao.midnightMillis
import `fun`.han1997.dianmao.minutesOfDay
import `fun`.han1997.dianmao.TaskSaveOutcome
import `fun`.han1997.dianmao.nextSubTaskId
import `fun`.han1997.dianmao.showDatePicker
import `fun`.han1997.dianmao.showTimePicker
import `fun`.han1997.dianmao.quietBorder
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddTaskPanel(
    visible: Boolean,
    editingTask: TaskUiModel?,
    initialPriority: TaskPriority,
    onDismiss: () -> Unit,
    onRequestCalendarPermission: () -> Unit,
    onSave: suspend (TaskFormState) -> TaskSaveOutcome,
    onStatus: (String) -> Unit
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
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var subTasksExpanded by rememberSaveable { mutableStateOf(false) }
    var repeatMenuExpanded by rememberSaveable { mutableStateOf(false) }
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val formScrollState = rememberScrollState()
    val titleFocusRequester = remember { FocusRequester() }
    val isEditing = editingTask != null
    val panelTitle = if (isEditing) "编辑事项" else "先记录一件事"
    val saveButtonText = if (isEditing) "保存修改并关闭" else "保存并继续记录"

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
            saveFailed = false
            isSaving = false
            subTasksExpanded = false
        }
    }

    // Auto-focus the title field with the keyboard on add-mode open.
    LaunchedEffect(visible, editingTask?.id) {
        if (visible && !isEditing) {
            kotlinx.coroutines.delay(220)
            titleFocusRequester.requestFocus()
        }
    }

    LaunchedEffect(saveFeedback) {
        if (saveFeedback != null && !saveFailed) {
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

    fun saveTask() {
        val trimmed = title.trim()
        if (trimmed.isNotEmpty() && !isSaving) {
            focusManager.clearFocus()
            keyboard?.hide()
            isSaving = true
            saveFeedback = null
            saveFailed = false
            scope.launch {
                val outcome = onSave(
                    TaskFormState(
                        title = trimmed,
                        quadrant = priority.type,
                        dueDateMillis = dueDateMillis,
                        dueTimeMinutes = dueTimeMinutes,
                        useCalendar = useCalendar,
                        useAlarm = useAlarm,
                        repeatRule = repeatRule,
                        subTasks = subTasks
                    ).normalizedTimeSettings()
                )
                isSaving = false
                saveFailed = !outcome.saved
                if (!outcome.saved) {
                    saveFeedback = outcome.message
                    return@launch
                }

                if (outcome.message != "事项已保存") {
                    onStatus(outcome.message)
                }
                if (isEditing) {
                    onDismiss()
                } else {
                    saveFeedback = "已保存到 ${priority.shortTitle}，可继续记录"
                    title = ""
                    dueDateMillis = null
                    dueTimeMinutes = null
                    useCalendar = false
                    useAlarm = false
                    repeatRule = RepeatRule.None
                    subTasks = emptyList()
                    subTaskDraft = ""
                    subTasksExpanded = false
                }
            }
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(280)) { it / 4 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 4 }
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
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f), CircleShape)
                    )
                    // Compact header: title + close only
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = panelTitle,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        HeaderActionButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = if (isEditing) "关闭编辑面板" else "关闭新增面板",
                            onClick = onDismiss
                        )
                    }
                    // Title field: auto-focused, 1-3 lines adaptive (sticky head)
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(titleFocusRequester),
                        placeholder = { Text(if (isEditing) "更新事项内容" else "要记录什么？") },
                        singleLine = false,
                        minLines = 1,
                        maxLines = 3,
                        shape = RowShape,
                        textStyle = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 18.sp,
                            lineHeight = 25.sp,
                            letterSpacing = 0.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { if (!isEditing) saveTask() else focusManager.clearFocus() }),
                        colors = quietTextFieldColors()
                    )
                    // Metadata chip row: quadrant / date / time / calendar / alarm / repeat
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
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
                        FilterChip(
                            selected = dueDateMillis != null,
                            onClick = {
                                showDatePicker(
                                    context = context,
                                    currentMillis = dueDateMillis,
                                    onSelected = { selected ->
                                        dueDateMillis = selected
                                        if (dueTimeMinutes == null) dueTimeMinutes = 9 * 60
                                    }
                                )
                            },
                            label = {
                                val currentDate = dueDateMillis
                                Text(
                                    if (currentDate == null) {
                                        "未设置"
                                    } else {
                                        Instant.ofEpochMilli(currentDate)
                                            .atZone(ZoneId.systemDefault())
                                            .toLocalDate()
                                            .format(DateTimeFormatter.ofPattern("M月d日"))
                                    }
                                )
                            },
                            leadingIcon = {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.CalendarMonth,
                                    tint = MaterialTheme.colorScheme.secondary
                                )
                            }
                        )
                        FilterChip(
                            selected = dueTimeMinutes != null,
                            enabled = dueDateMillis != null,
                            onClick = {
                                showTimePicker(
                                    context = context,
                                    currentMinutes = dueTimeMinutes,
                                    onSelected = { dueTimeMinutes = it }
                                )
                            },
                            label = {
                                val currentTime = dueTimeMinutes
                                Text(
                                    if (currentTime == null) {
                                        "未设置"
                                    } else {
                                        val hour = currentTime / 60
                                        val minute = currentTime % 60
                                        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                                    }
                                )
                            },
                            leadingIcon = {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.Schedule,
                                    tint = if (dueDateMillis != null) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    }
                                )
                            }
                        )
                        FilterChip(
                            selected = useCalendar,
                            enabled = dueDateMillis != null,
                            onClick = {
                                useCalendar = !useCalendar
                                if (useCalendar) onRequestCalendarPermission()
                            },
                            label = { Text(if (useCalendar) "日历" else "未设置") },
                            leadingIcon = {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.CalendarMonth,
                                    tint = if (dueDateMillis != null) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    }
                                )
                            }
                        )
                        FilterChip(
                            selected = useAlarm,
                            enabled = dueDateMillis != null,
                            onClick = {
                                useAlarm = !useAlarm
                                if (!useAlarm) repeatRule = RepeatRule.None
                            },
                            label = { Text(if (useAlarm) "响铃" else "未设置") },
                            leadingIcon = {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.Alarm,
                                    tint = if (dueDateMillis != null) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    }
                                )
                            }
                        )
                        FilterChip(
                            selected = repeatRule != RepeatRule.None,
                            enabled = dueDateMillis != null && useAlarm,
                            onClick = { repeatMenuExpanded = true },
                            label = { Text(if (repeatRule == RepeatRule.None) "未设置" else repeatRule.label) },
                            leadingIcon = {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.Repeat,
                                    tint = if (dueDateMillis != null && useAlarm) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                                    }
                                )
                            }
                        )
                    }
                    DropdownMenu(
                        expanded = repeatMenuExpanded,
                        onDismissRequest = { repeatMenuExpanded = false }
                    ) {
                        RepeatRule.entries.forEach { rule ->
                            DropdownMenuItem(
                                text = { Text(rule.label) },
                                leadingIcon = if (rule == repeatRule) {
                                    {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    null
                                },
                                onClick = {
                                    repeatRule = rule
                                    repeatMenuExpanded = false
                                }
                            )
                        }
                    }
                    AnimatedVisibility(
                        visible = saveFeedback != null,
                        enter = fadeIn(tween(140)) + slideInVertically(tween(180)) { it / 3 },
                        exit = fadeOut(tween(160))
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RowShape,
                            color = if (saveFailed) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.66f)
                            } else {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.54f)
                            },
                            contentColor = if (saveFailed) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            },
                            border = quietBorder(0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IconTile(
                                    icon = if (saveFailed) Icons.Rounded.Close else Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = if (saveFailed) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                    containerColor = if (saveFailed) {
                                        MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.10f)
                                    } else {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
                                    },
                                    iconSize = 14.dp
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
                    // Save button pinned at the bottom of the sticky head
                    Button(
                        onClick = { saveTask() },
                        enabled = title.isNotBlank() && !isSaving,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RowShape,
                        contentPadding = PaddingValues(vertical = 13.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        InlineButtonIcon(
                            icon = Icons.Rounded.Check,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(if (isSaving) "正在保存…" else saveButtonText)
                    }
                    // Collapsed detail body: expandable 子事项 section (default collapsed)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(formScrollState),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RowShape,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                            border = quietBorder(0.10f)
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 48.dp)
                                        .clickable(role = Role.Button) { subTasksExpanded = !subTasksExpanded }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.CheckCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (subTasks.isEmpty()) {
                                            "子事项"
                                        } else {
                                            "子事项 · ${subTasks.size}"
                                        },
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = if (subTasksExpanded) {
                                            Icons.Rounded.KeyboardArrowUp
                                        } else {
                                            Icons.Rounded.KeyboardArrowDown
                                        },
                                        contentDescription = if (subTasksExpanded) "收起子事项" else "展开子事项",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                AnimatedVisibility(
                                    visible = subTasksExpanded,
                                    enter = fadeIn(tween(220)) + slideInVertically(tween(220)) { it / 4 },
                                    exit = fadeOut(tween(160)) + slideOutVertically(tween(180)) { it / 4 }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                                    ) {
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
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
