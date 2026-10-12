package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons

import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskCompletionEntity
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.data.repeatRuleLabel
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
    onUpdateCompletionNote: (Long, String?) -> Unit,
    onOpenHistory: (TaskUiModel) -> Unit = {}
) {
    val completions by repository.observeCompletions(task.id).collectAsState(initial = emptyList())
    val listState = rememberLazyListState()

    FullScreenScaffold(
        title = "事项详情",
        subtitle = task.quadrant.shortTitle,
        leadingIcon = DianMaoIcons.Inventory,
        leadingIconTint = task.quadrant.color,
        onBack = onBack,
        actions = {
            HeaderActionButton(
                icon = DianMaoIcons.Edit,
                contentDescription = "编辑",
                onClick = onEdit,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 2.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f),
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
                        ColorDot(task.quadrant.color)
                        Text(
                            text = task.quadrant.shortTitle,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 0.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Detail body: the task's own description, above the meta rows
                if (!task.detail.isNullOrBlank()) {
                    item {
                        Text(
                            text = task.detail.orEmpty(),
                            modifier = Modifier.padding(start = 58.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Meta section: full-width verb rows (To Do verb-becomes-value pattern)
                item {
                    TaskFormSection(
                        title = "事项信息",
                        icon = DianMaoIcons.Inventory,
                        iconTint = task.quadrant.color
                    ) {
                        DetailMetaRow(
                            icon = DianMaoIcons.Schedule,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.dueLabel != null) task.dueLabel else "添加提醒"
                        )
                        DetailMetaRow(
                            icon = DianMaoIcons.Calendar,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.useCalendar) "已同步系统日历" else "未同步系统日历"
                        )
                        DetailMetaRow(
                            icon = DianMaoIcons.Alarm,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.useAlarm) "响铃提醒已开启" else "未设置响铃"
                        )
                        DetailMetaRow(
                            icon = DianMaoIcons.Bell,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            // Prototype-first: the detail meta row shows only the
                            // on/off state; lead time lives in the edit panel.
                            label = if (task.useLocalReminder) {
                                "到点提醒已开启"
                            } else {
                                "未开启到点提醒"
                            }
                        )
                        DetailMetaRow(
                            icon = DianMaoIcons.Repeat,
                            iconTint = MaterialTheme.colorScheme.secondary,
                            label = if (task.repeatRule != RepeatRule.None) {
                                repeatRuleLabel(task.repeatRule, task.repeatInterval, task.repeatWeekdays)
                            } else {
                                "不重复"
                            }
                        )
                        if (task.completionCount > 0) {
                            DetailMetaRow(
                                icon = DianMaoIcons.CheckCircle,
                                iconTint = task.quadrant.color,
                                label = "已完成 ${task.completionCount} 次",
                                onClick = { onOpenHistory(task) }
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
                            contentPadding = PaddingValues(vertical = 11.dp)
                        ) {
                            InlineButtonIcon(
                                icon = DianMaoIcons.Calendar,
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
                            icon = DianMaoIcons.EditNote,
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
                                                MaterialTheme.colorScheme.onSurfaceVariant,
                                                CircleShape
                                            )
                                    )
                                    Text(
                                        text = "${DateTimeFormatters.formatTimestamp(it)} 上次完成",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
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

            // Bottom action bar: actions sit directly on the page background
            // (no tonal frame), matching the add/edit form's save button.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { onToggleComplete(task) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RowShape,
                    contentPadding = PaddingValues(vertical = 13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    InlineButtonIcon(
                        icon = if (task.completed) DianMaoIcons.Replay else DianMaoIcons.DoneAll,
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
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        ) {
                            InlineButtonIcon(
                                icon = DianMaoIcons.Archive,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
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
                    HeaderActionButton(
                        icon = DianMaoIcons.Delete,
                        contentDescription = "删除",
                        onClick = { onDelete(task) },
                        tint = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f)
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailMetaRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 36.dp)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button, onClick = onClick)
                } else {
                    Modifier
                }
            ),
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
        if (onClick != null) {
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = DianMaoIcons.ChevRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.muted),
                modifier = Modifier.size(18.dp)
            )
        }
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
                    imageVector = DianMaoIcons.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.muted),
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "完成历史",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                CountLabel(
                    count = completions.size,
                    suffix = "",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                )
                Icon(
                    imageVector = if (expanded) {
                        DianMaoIcons.ChevUp
                    } else {
                        DianMaoIcons.ChevDown
                    },
                    contentDescription = if (expanded) "收起完成历史" else "展开完成历史",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                    modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(
                visible = expanded,
                enter = (fadeIn(tween(200)) + slideInVertically(tween(220)) { it / 4 }).withReducedMotion(),
                exit = (fadeOut(tween(160)) + slideOutVertically(tween(180)) { it / 4 }).withReducedMotion()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (completions.isEmpty()) {
                        CompletionHistoryEmptyHint(
                            isRecurring = isRecurring,
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

/**
 * Shared completion-history empty copy (ui-polish round 2). The inline
 * collapsible group and the full-screen history panel used two slightly
 * different strings ("," vs "，"); both now render through this hint so the
 * copy cannot drift again.
 */
@Composable
internal fun CompletionHistoryEmptyHint(
    isRecurring: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        text = if (isRecurring) "完成循环事项后，这里会记录每次完成" else "暂无完成记录",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText),
        modifier = modifier
    )
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RowShape,
        border = quietBorder(0.12f),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    HeaderActionButton(
                        icon = DianMaoIcons.Edit,
                        contentDescription = "编辑备注",
                        onClick = {
                            editNoteText = completion.note.orEmpty()
                            isEditingNote = true
                        },
                        iconSize = 16.dp
                    )
                    HeaderActionButton(
                        icon = DianMaoIcons.Delete,
                        contentDescription = "删除",
                        onClick = { showDeleteDialog = true },
                        tint = MaterialTheme.colorScheme.error,
                        containerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.10f),
                        iconSize = 16.dp
                    )
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
        QuietDialogContainer(
            onDismiss = { showDeleteDialog = false },
            title = "删除完成记录",
            leadingIcon = DianMaoIcons.Delete,
            leadingIconTint = MaterialTheme.colorScheme.error,
            actions = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            }
        ) {
            Text("确定要删除这条完成记录吗？")
        }
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
        leadingIcon = DianMaoIcons.History,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onDismiss,
        actions = {
            HeaderActionButton(
                icon = DianMaoIcons.Edit,
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
                        icon = DianMaoIcons.CheckCircle,
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
                            text = repeatRuleLabel(task.repeatRule, task.repeatInterval, task.repeatWeekdays),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
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
                    icon = DianMaoIcons.DoneAll,
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
                            icon = DianMaoIcons.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.muted),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                            iconSize = 22.dp,
                            modifier = Modifier.size(44.dp)
                        )
                        CompletionHistoryEmptyHint(
                            isRecurring = task.repeatRule != RepeatRule.None
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
