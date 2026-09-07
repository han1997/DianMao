package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskFormState
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.normalizedTimeSettings
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.DianMaoDatePickerDialog
import `fun`.han1997.dianmao.DianMaoTimePickerDialog
import `fun`.han1997.dianmao.FullScreenScaffold
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.SubTaskEditor
import `fun`.han1997.dianmao.midnightMillis
import `fun`.han1997.dianmao.minutesOfDay
import `fun`.han1997.dianmao.TaskSaveOutcome
import `fun`.han1997.dianmao.nextSubTaskId
import `fun`.han1997.dianmao.quietBorder
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
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
    var useLocalReminder by rememberSaveable { mutableStateOf(false) }
    var reminderLeadMinutes by rememberSaveable { mutableStateOf(0) }
    var repeatRule by rememberSaveable { mutableStateOf(RepeatRule.None) }
    var subTasks by remember { mutableStateOf<List<TaskSubTask>>(emptyList()) }
    var subTaskDraft by rememberSaveable { mutableStateOf("") }
    var saveFeedback by rememberSaveable { mutableStateOf<String?>(null) }
    var saveFailed by rememberSaveable { mutableStateOf(false) }
    var isSaving by rememberSaveable { mutableStateOf(false) }
    var repeatMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var leadMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showDatePickerDialog by rememberSaveable { mutableStateOf(false) }
    var showTimePickerDialog by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val formScrollState = rememberScrollState()
    val titleFocusRequester = remember { FocusRequester() }
    val isEditing = editingTask != null
    val panelTitle = if (isEditing) "编辑事项" else "新增事项"
    val saveButtonText = if (isEditing) "保存修改并关闭" else "保存并关闭"

    LaunchedEffect(visible, editingTask?.id, initialPriority) {
        if (visible) {
            title = editingTask?.title.orEmpty()
            priority = editingTask?.quadrant ?: initialPriority
            dueDateMillis = editingTask?.dueAtMillis?.let { midnightMillis(it) }
            dueTimeMinutes = editingTask?.dueAtMillis?.let { minutesOfDay(it) }
            useCalendar = editingTask?.useCalendar ?: false
            useAlarm = editingTask?.useAlarm ?: false
            useLocalReminder = editingTask?.useLocalReminder ?: false
            reminderLeadMinutes = editingTask?.reminderLeadMinutes ?: 0
            repeatRule = editingTask?.repeatRule ?: RepeatRule.None
            subTasks = editingTask?.subTasks ?: emptyList()
            subTaskDraft = ""
            saveFeedback = null
            saveFailed = false
            isSaving = false
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
                        useLocalReminder = useLocalReminder,
                        reminderLeadMinutes = reminderLeadMinutes,
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
                // Both add and edit close after a successful save — capture
                // is single-shot, no continuous-recording mode.
                onDismiss()
            }
        }
    }

    if (visible) {
        FullScreenScaffold(
            title = panelTitle,
            subtitle = if (isEditing) "更新事项内容与安排" else "先记录一件事，稍后再分类整理",
            leadingIcon = if (isEditing) Icons.Rounded.Edit else Icons.Rounded.Add,
            leadingIconTint = MaterialTheme.colorScheme.primary,
            onBack = onDismiss,
            actions = {
                HeaderActionButton(
                    icon = Icons.Rounded.Close,
                    contentDescription = if (isEditing) "关闭编辑页面" else "关闭新增页面",
                    onClick = onDismiss
                )
            }
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Scrollable form body: capture field, metadata chips, feedback, subtasks.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(formScrollState),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Title field: auto-focused in add mode, 1-3 lines adaptive.
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(titleFocusRequester),
                    placeholder = { Text(if (isEditing) "更新事项内容" else "要记录什么?") },
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

                // Priority section: quadrant choice as one connected segmented row.
                TaskFormSection(
                    title = "优先级",
                    subtitle = "放入四象限，决定它的颜色与归类",
                    icon = Icons.Rounded.Inventory2,
                    iconTint = priority.color
                ) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TaskPriority.entries.forEachIndexed { index, item ->
                            SegmentedButton(
                                selected = item == priority,
                                onClick = { priority = item },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = TaskPriority.entries.size
                                ),
                                modifier = Modifier.weight(1f),
                                label = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        ColorDot(item.color, size = 8.dp)
                                        Text(item.shortTitle, maxLines = 1)
                                    }
                                },
                                colors = SegmentedButtonDefaults.colors(
                                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // Time & reminder section: five self-explanatory setting rows.
                TaskFormSection(
                    title = "时间与提醒",
                    subtitle = "到期日决定时间、日历、响铃与重复是否可用",
                    icon = Icons.Rounded.Schedule,
                    iconTint = MaterialTheme.colorScheme.secondary
                ) {
                    val hasDate = dueDateMillis != null
                    val dateText = dueDateMillis?.let {
                        Instant.ofEpochMilli(it)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                            .format(DateTimeFormatter.ofPattern("M月d日 EEEE"))
                    }

                    // 到期日
                    SettingRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = dateText ?: "添加到期日",
                        subtitle = if (dateText != null) {
                            "点按修改到期日"
                        } else {
                            "为该事项选一天"
                        },
                        iconTint = MaterialTheme.colorScheme.secondary,
                        onClick = { showDatePickerDialog = true }
                    )
                    SettingDivider()

                    // 提醒时间
                    val timeText = dueTimeMinutes?.let { minutes ->
                        val hour = minutes / 60
                        val minute = minutes % 60
                        "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"
                    }
                    SettingRow(
                        icon = Icons.Rounded.Schedule,
                        title = timeText ?: "添加提醒时间",
                        subtitle = when {
                            timeText != null -> "点按修改提醒时间"
                            !hasDate -> "需先设置到期日"
                            else -> "选择当天提醒的几点几分"
                        },
                        iconTint = if (hasDate) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        enabled = hasDate,
                        onClick = { showTimePickerDialog = true }
                    )
                    SettingDivider()

                    // 同步系统日历 (toggle, master switch)
                    SettingRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = if (useCalendar) "已同步到系统日历" else "同步到系统日历",
                        subtitle = when {
                            useCalendar -> "到期日会写入系统日历"
                            !hasDate -> "需先设置到期日"
                            else -> "在系统日历中创建日程"
                        },
                        iconTint = if (useCalendar) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        enabled = hasDate,
                        onClick = {
                            useCalendar = !useCalendar
                            if (!useCalendar) useAlarm = false
                            if (useCalendar) onRequestCalendarPermission()
                        },
                        trailing = {
                            Switch(
                                checked = useCalendar,
                                onCheckedChange = null,
                                enabled = hasDate,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    )
                    SettingDivider()

                    // 日历响铃 (toggle, sub-switch of 同步到系统日历)
                    val alarmReady = hasDate && useCalendar
                    SettingRow(
                        icon = Icons.Rounded.Alarm,
                        title = if (useAlarm) "日历响铃已开启" else "开启日历响铃",
                        subtitle = when {
                            useAlarm -> "日程到点在系统日历响铃"
                            !hasDate -> "需先设置到期日"
                            !useCalendar -> "需先同步到系统日历"
                            else -> "开启后系统日历日程带响铃"
                        },
                        iconTint = if (useAlarm) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        enabled = alarmReady,
                        onClick = {
                            useAlarm = !useAlarm
                            if (!useAlarm) repeatRule = RepeatRule.None
                        },
                        trailing = {
                            Switch(
                                checked = useAlarm,
                                onCheckedChange = null,
                                enabled = alarmReady,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    )
                    SettingDivider()

                    // 到点提醒 (toggle, DianMao's own local notification)
                    val reminderReady = hasDate
                    SettingRow(
                        icon = Icons.Rounded.NotificationsActive,
                        title = if (useLocalReminder) "到点提醒已开启" else "开启到点提醒",
                        subtitle = when {
                            useLocalReminder -> leadLabel(reminderLeadMinutes) + "提醒我"
                            !hasDate -> "需先设置到期日"
                            else -> "在 DianMao 内发出通知，不依赖日历"
                        },
                        iconTint = if (useLocalReminder) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        enabled = reminderReady,
                        onClick = {
                            useLocalReminder = !useLocalReminder
                        },
                        trailing = {
                            Switch(
                                checked = useLocalReminder,
                                onCheckedChange = null,
                                enabled = reminderReady,
                                colors = SwitchDefaults.colors(
                                    checkedTrackColor = MaterialTheme.colorScheme.secondary,
                                    checkedThumbColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    )
                    SettingDivider()

                    // 提醒提前量 (sub-option of 到点提醒)
                    val reminderLeadReady = hasDate && useLocalReminder
                    SettingRow(
                        icon = Icons.Rounded.Schedule,
                        title = leadLabel(reminderLeadMinutes),
                        subtitle = when {
                            !hasDate -> "需先设置到期日"
                            !useLocalReminder -> "需先开启到点提醒"
                            else -> "点按修改提醒提前量"
                        },
                        iconTint = if (reminderLeadReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        enabled = reminderLeadReady,
                        onClick = { leadMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = leadMenuExpanded,
                        onDismissRequest = { leadMenuExpanded = false }
                    ) {
                        ReminderLeadOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                leadingIcon = if (option.minutes == reminderLeadMinutes) {
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
                                    reminderLeadMinutes = option.minutes
                                    leadMenuExpanded = false
                                }
                            )
                        }
                    }
                    SettingDivider()

                    // 重复
                    Box {
                        val repeatReady = hasDate && useCalendar && useAlarm
                        val repeatText = if (repeatRule == RepeatRule.None) null else repeatRule.label
                        SettingRow(
                            icon = Icons.Rounded.Repeat,
                            title = repeatText ?: "不重复",
                            subtitle = when {
                                repeatText != null -> "点按修改重复规则"
                                !hasDate -> "需先设置到期日"
                                !useCalendar -> "需先同步到系统日历"
                                !useAlarm -> "需先开启日历响铃"
                                else -> "可设为每天、每周或每月"
                            },
                            iconTint = if (repeatReady) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                            enabled = repeatReady,
                            onClick = { repeatMenuExpanded = true }
                        )
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
                    }
                }

                // Save feedback pill (add-mode confirmation / save failure).
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

                // Subtask editor: a grouped TaskFormSection, always visible on the full screen.
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

            // Bottom action bar: the primary save button sits directly on the
            // page background (no tonal frame), pushed above the IME.
            Button(
                onClick = { saveTask() },
                enabled = title.isNotBlank() && !isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
                    .imePadding(),
                shape = RowShape,
                contentPadding = PaddingValues(vertical = 12.dp, horizontal = 18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                InlineButtonIcon(
                    icon = Icons.Rounded.Check,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (isSaving) "正在保存..." else saveButtonText)
            }
        }
        }

        if (showDatePickerDialog) {
            DianMaoDatePickerDialog(
                selectedDateMillis = dueDateMillis,
                onDismiss = { showDatePickerDialog = false },
                onConfirm = { selected ->
                    dueDateMillis = selected.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    if (dueTimeMinutes == null) dueTimeMinutes = 9 * 60
                    showDatePickerDialog = false
                }
            )
        }

        if (showTimePickerDialog) {
            DianMaoTimePickerDialog(
                initialMinutesOfDay = dueTimeMinutes,
                onDismiss = { showTimePickerDialog = false },
                onConfirm = { selected ->
                    dueTimeMinutes = selected
                    showTimePickerDialog = false
                }
            )
        }
    }
}

/**
 * Interactive verb→value row used inside the 时间与提醒 setting section.
 *
 * Mirrors the task-detail "verb becomes value" dialect: the [title] is the
 * action verb while unset and the current value once set; [subtitle] carries
 * one line of explanation (or the dependency reason when [enabled] is false).
 * Toggle rows pass a [trailing] [Switch]; value rows fall back to a quiet
 * chevron.
 */
@Composable
private fun SettingRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailing: (@Composable () -> Unit)? = null
) {
    val mutedTint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.72f else 0.5f)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable(
                enabled = enabled,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(
            icon = icon,
            contentDescription = null,
            tint = iconTint,
            containerColor = iconTint.copy(alpha = 0.10f),
            iconSize = 17.dp
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = if (enabled) FontWeight.Medium else FontWeight.Normal,
                    letterSpacing = 0.sp
                ),
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                color = mutedTint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = if (enabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                },
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SettingDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, end = 4.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    )
}

/** Reminder lead-time choices shown in the 提醒提前量 menu. */
internal enum class ReminderLeadOption(val minutes: Int, val label: String) {
    OnTime(0, "准时提醒"),
    FiveMinutesBefore(5, "提前 5 分钟"),
    ThirtyMinutesBefore(30, "提前 30 分钟"),
    OneDayBefore(24 * 60, "提前 1 天")
}

internal fun leadLabel(minutes: Int): String {
    return ReminderLeadOption.entries.firstOrNull { it.minutes == minutes }?.label ?: "准时提醒"
}
