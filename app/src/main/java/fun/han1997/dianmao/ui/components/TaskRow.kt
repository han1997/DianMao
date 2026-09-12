package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.CompactMeta
import `fun`.han1997.dianmao.CompletionMark
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.quietBorder
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TaskRow(
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
    isDragged: Boolean = false,
    dragOffsetPx: Float = 0f,
    modifier: Modifier = Modifier
) {
    var canComplete by remember { mutableStateOf(true) }
    var actionsExpanded by remember { mutableStateOf(false) }
    val haptics = LocalHapticFeedback.current
    val displayedDueLabel = dueLabelOverride ?: task.dueLabel
    val scale by animateFloatAsState(
        targetValue = if (task.completed) 0.985f else 1f,
        animationSpec = motionTween(durationMillis = 180),
        label = "taskScale"
    )

    LaunchedEffect(task.id, task.lastCompletedAtMillis, task.completed, task.repeatRule) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Drag lift: instant 8dp shadow + 1.02 scale while held, and
                // the row tracks the finger vertically with no tween (the
                // Finger-Tracked rule). At rest both stay at their defaults.
                if (isDragged) {
                    shadowElevation = 8.dp.toPx()
                    scaleX = 1.02f
                    scaleY = 1.02f
                    translationY = dragOffsetPx
                }
            }
            .scale(scale),
        colors = CardDefaults.cardColors(
            containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RowShape,
        border = quietBorder(if (task.completed) 0.12f else 0.16f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(
                        enabled = canComplete || task.completed,
                        role = Role.Checkbox
                    ) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleComplete(task)
                    }
                    .semantics {
                        contentDescription = if (task.completed) "标记为未完成" else "标记为完成"
                        stateDescription = if (task.completed) "已完成" else "未完成"
                    },
                contentAlignment = Alignment.TopCenter
            ) {
                CompletionMark(
                    completed = task.completed,
                    color = task.quadrant.color
                )
            }
            Spacer(Modifier.width(2.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(role = Role.Button) { onViewDetail(task) }
                    .semantics {
                        contentDescription = "查看事项详情：${task.title}"
                    },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 15.sp,
                        letterSpacing = 0.sp
                    ),
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong) else MaterialTheme.colorScheme.onSurface,
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
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (showQuadrantMeta) {
                        CompactMeta(
                            icon = Icons.Rounded.Inventory2,
                            text = task.quadrant.shortTitle,
                            tint = task.quadrant.color
                        )
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
                        CompactMeta(icon = Icons.Rounded.Repeat, text = task.repeatRule.label)
                    }
                    if (task.subTasks.isNotEmpty()) {
                        CompactMeta(icon = Icons.Rounded.CheckCircle, text = task.subTaskProgressLabel)
                    }
                    if (task.completionNote != null) {
                        CompactMeta(icon = Icons.AutoMirrored.Rounded.Notes, text = "有心得")
                    }
                }
            }
            Box {
                    HeaderActionButton(
                        icon = Icons.Rounded.MoreVert,
                        contentDescription = "更多事项操作",
                        onClick = { actionsExpanded = true },
                        iconSize = 18.dp
                    )
                    QuietDropdownMenu(
                        expanded = actionsExpanded,
                        onDismissRequest = { actionsExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(if (task.completed) "编辑完成备注" else "完成并写备注") },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (task.completed) Icons.Rounded.EditNote else Icons.Rounded.DoneAll,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                actionsExpanded = false
                                onEditNote(task)
                            }
                        )
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

@Composable
internal fun SubTaskProgressSummary(
    completed: Int,
    total: Int,
    color: Color
) {
    val progress = if (total == 0) 0f else completed.toFloat() / total.toFloat()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                .background(MaterialTheme.colorScheme.surfaceVariant, PillShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                    .height(4.dp)
                    .background(color.copy(alpha = DianMaoAlpha.strong), PillShape)
            )
        }
    }
}

@Composable
fun rememberDragDropListState(
    lazyListState: LazyListState,
    onMove: (Int, Int) -> Unit
): DragDropListState {
    // Hold onMove in rememberUpdatedState so the drag callbacks always invoke
    // the latest composition's lambda — a plain remember{} would freeze the
    // first-composition closure with stale counts and silently break reorder.
    val currentOnMove by rememberUpdatedState(onMove)
    val state = remember { DragDropListState(lazyListState) }
    state.onMoveHandler = { from, to -> currentOnMove(from, to) }
    return state
}

class DragDropListState(
    private val lazyListState: LazyListState
) {
    var draggedIndex by mutableStateOf<Int?>(null)
    var draggedOffset by mutableStateOf(0f)

    /**
     * Count of non-task items before the first task row (header + intro +
     * section label). Set by the screen so relative task indices translate
     * to absolute LazyColumn indices via [taskIndexPrefix].
     */
    var taskIndexPrefix: Int = 0
    var openTaskCount: Int = 0
    var onMoveHandler: ((Int, Int) -> Unit)? = null

    fun onDragStart(index: Int) {
        draggedIndex = index
    }

    fun onDrag(delta: Float) {
        draggedOffset += delta

        val draggedIdx = draggedIndex ?: return
        val startOffset = draggedOffset
        // draggedIdx is a relative task index; visibleItemsInfo uses absolute
        // LazyColumn indices, so translate before comparing.
        val absoluteDragged = draggedIdx + taskIndexPrefix

        val currentElement = lazyListState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.index == absoluteDragged }
        val targetElement = lazyListState.layoutInfo.visibleItemsInfo.find { info ->
            val targetCenter = info.offset + info.size / 2
            val draggedCenter = (currentElement?.offset ?: 0) + startOffset + (currentElement?.size ?: 0) / 2

            if (startOffset > 0) {
                draggedCenter > targetCenter && info.index > absoluteDragged
            } else {
                draggedCenter < targetCenter && info.index < absoluteDragged
            }
        } ?: return

        // Only task rows inside the open section are valid move targets.
        val targetTaskIndex = targetElement.index - taskIndexPrefix
        if (targetTaskIndex !in 0 until openTaskCount) return

        if (targetTaskIndex != draggedIdx) {
            onMoveHandler?.invoke(draggedIdx, targetTaskIndex)
            draggedIndex = targetTaskIndex
            draggedOffset = 0f
        }
    }

    fun onDragEnd() {
        draggedIndex = null
        draggedOffset = 0f
    }
}
