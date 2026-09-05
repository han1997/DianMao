package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskCompletionEntity
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.CompletionMark
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.InlineStatDot
import `fun`.han1997.dianmao.SubTaskReadOnlySection
import `fun`.han1997.dianmao.TaskFormSection
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
internal fun TaskDetailScreen(
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
    val listState = rememberLazyListState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        // The edge-to-edge Scaffold leaves each screen responsible for its safe area.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                border = quietBorder(0.10f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HeaderActionButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "返回",
                        onClick = onBack
                    )
                    Text(
                        text = "事项详情",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    HeaderActionButton(
                        icon = Icons.Rounded.Edit,
                        contentDescription = "编辑",
                        onClick = onEdit,
                        tint = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Hero: completion checkbox + title on one line (To Do/Things pattern)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clickable(
                                    role = Role.Checkbox
                                ) { onToggleComplete(task) }
                                .semantics {
                                    contentDescription = if (task.completed) "标记为未完成" else "标记为完成"
                                },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            CompletionMark(
                                completed = task.completed,
                                color = task.quadrant.color
                            )
                        }
                        Text(
                            text = task.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (task.completed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }

                // Quadrant chip beneath the title (12dp dot + shortTitle)
                item {
                    Row(
                        modifier = Modifier.padding(start = 58.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(task.quadrant.color, CircleShape)
                        )
                        Text(
                            text = task.quadrant.shortTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.05.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Meta section: full-width verb rows (To Do verb-becomes-value pattern)
                item {
                    TaskFormSection(
                        title = "事项信息",
                        icon = Icons.Rounded.Inventory2,
                        iconTint = task.quadrant.color
                    ) {
                        DetailMetaRow(
                            icon = Icons.Rounded.Schedule,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.dueLabel != null) task.dueLabel else "添加提醒"
                        )
                        DetailMetaRow(
                            icon = Icons.Rounded.CalendarMonth,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.useCalendar) "已同步系统日历" else "未同步系统日历"
                        )
                        DetailMetaRow(
                            icon = Icons.Rounded.Alarm,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.useAlarm) "响铃提醒已开启" else "未设置响铃"
                        )
                        DetailMetaRow(
                            icon = Icons.Rounded.Repeat,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.repeatRule != RepeatRule.None) {
                                task.repeatRule.label
                            } else {
                                "不重复"
                            }
                        )
                        if (task.completionCount > 0) {
                            DetailMetaRow(
                                icon = Icons.Rounded.CheckCircle,
                                iconTint = task.quadrant.color,
                                label = "已完成 ${task.completionCount} 次"
                            )
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
                        OutlinedButton(
                            onClick = { onOpenCalendarReminder(task) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RowShape,
                            contentPadding = PaddingValues(vertical = 13.dp)
                        ) {
                            InlineButtonIcon(
                                icon = Icons.Rounded.CalendarMonth,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("打开系统日历提醒")
                        }
                    }
                }

                // Completion note (if exists)
                task.completionNote?.takeIf { it.isNotBlank() }?.let { note ->
                    item {
                        TaskFormSection(
                            title = "完成心得",
                            icon = Icons.Rounded.EditNote,
                            iconTint = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = note,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Stats row
                if (task.repeatRule != RepeatRule.None || completions.isNotEmpty()) {
                    item {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            InlineStatDot(
                                count = task.completionCount,
                                label = "总完成次数",
                                color = task.quadrant.color
                            )
                            InlineStatDot(
                                count = completions.size,
                                label = "历史记录",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            task.lastCompletedAtMillis?.let {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(
                                                MaterialTheme.colorScheme.secondary,
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = "${DateTimeFormatters.formatTimestamp(it)} 上次完成",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Completion history: collapsed group at the bottom (Google Tasks pattern)
                item {
                    CompletionHistoryGroup(
                        completions = completions,
                        isRecurring = task.repeatRule != RepeatRule.None,
                        onDelete = onDeleteCompletion,
                        onUpdateNote = onUpdateCompletionNote
                    )
                }
            }

            // Bottom action bar
            Surface(
                tonalElevation = 2.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onToggleComplete(task) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RowShape,
                        contentPadding = PaddingValues(vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        InlineButtonIcon(
                            icon = if (task.completed) Icons.Rounded.Replay else Icons.Rounded.DoneAll,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (task.completed) "标为未完成" else "完成")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (task.completed) {
                            FilledTonalButton(
                                onClick = { onArchive(task) },
                                modifier = Modifier.weight(1f),
                                shape = RowShape,
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            ) {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.Archive,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.10f)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("归档")
                            }
                        } else {
                            Text(
                                text = "删除会先要求确认",
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onDelete(task) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconTile(
                                icon = Icons.Rounded.Delete,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                iconSize = 20.dp,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DetailMetaRow(
    icon: ImageVector,
    iconTint: Color,
    label: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IconTile(
            icon = icon,
            contentDescription = null,
            tint = iconTint,
            containerColor = iconTint.copy(alpha = 0.10f),
            iconSize = 16.dp
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.86f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun CompletionHistoryGroup(
    completions: List<TaskCompletionEntity>,
    isRecurring: Boolean,
    onDelete: (Long) -> Unit,
    onUpdateNote: (Long, String?) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        shape = RowShape,
        border = quietBorder(0.12f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .clickable(
                        role = Role.Button
                    ) { expanded = !expanded }
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.64f),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "完成历史",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.05.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = completions.size.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
                )
                Icon(
                    imageVector = if (expanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = if (expanded) "收起完成历史" else "展开完成历史",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 4 },
                exit = fadeOut(tween(160)) + slideOutVertically(tween(180)) { it / 4 }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (completions.isEmpty()) {
                        Text(
                            text = if (isRecurring) "完成循环事项后这里会记录每次完成" else "暂无完成记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        )
                    } else {
                        completions.forEach { completion ->
                            CompletionItem(
                                completion = completion,
                                onDelete = { onDelete(completion.id) },
                                onUpdateNote = onUpdateNote
                            )
                        }
                    }
                }
            }
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
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            editNoteText = completion.note.orEmpty()
                            isEditingNote = true
                        },
                        modifier = Modifier.size(48.dp)
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
                        modifier = Modifier.size(48.dp)
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
    var canComplete by remember { mutableStateOf(false) }

    LaunchedEffect(task) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }

    FullScreenScaffold(
        title = "完成历史",
        subtitle = task.title,
        leadingIcon = Icons.Rounded.History,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onDismiss,
        actions = {
            HeaderActionButton(
                icon = Icons.Rounded.Edit,
                contentDescription = "编辑",
                onClick = { onEdit(task) },
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Task info
            Surface(
                color = task.quadrant.color.copy(alpha = 0.08f),
                shape = RowShape,
                border = quietBorder(0.12f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    IconTile(
                        icon = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = task.quadrant.color,
                        containerColor = task.quadrant.color.copy(alpha = 0.10f),
                        iconSize = 16.dp
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = task.quadrant.shortTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp
                            ),
                            color = task.quadrant.color.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = task.repeatRule.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${task.completionCount}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "总完成",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "${completions.size}",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "历史",
                            style = MaterialTheme.typography.labelMedium.copy(
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                        )
                    }
                }
            }

            // Complete button
            Button(
                onClick = { onComplete(task) },
                enabled = canComplete,
                modifier = Modifier.fillMaxWidth(),
                shape = RowShape,
                contentPadding = PaddingValues(vertical = 13.dp)
            ) {
                InlineButtonIcon(
                    icon = Icons.Rounded.DoneAll,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (canComplete) "完成本次" else "本周期已完成")
            }

            // History list
            if (completions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconTile(
                            icon = Icons.Rounded.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                            iconSize = 22.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = if (task.repeatRule != RepeatRule.None) {
                                "完成循环事项后,这里会记录每次完成"
                            } else {
                                "暂无完成记录"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(completions, key = { it.id }) { completion ->
                        CompletionItem(
                            completion = completion,
                            onDelete = { onDeleteCompletion(completion.id) },
                            onUpdateNote = onUpdateCompletionNote
                        )
                    }
                }
            }
        }
    }
}
