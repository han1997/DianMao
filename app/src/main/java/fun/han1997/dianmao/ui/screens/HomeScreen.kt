package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.AppCompassMark
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.CompactMeta
import `fun`.han1997.dianmao.CompletionMark
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.SummaryPill
import `fun`.han1997.dianmao.TaskRow
import `fun`.han1997.dianmao.HomeSmartTaskItem
import `fun`.han1997.dianmao.HomeSmartView
import `fun`.han1997.dianmao.TimeFocusItem
import `fun`.han1997.dianmao.TimeFocusKind
import `fun`.han1997.dianmao.buildTimeFocusItems
import `fun`.han1997.dianmao.quietBorder
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun AppHeader(
    tasks: List<TaskUiModel>,
    searchVisible: Boolean,
    query: String,
    onSearchToggle: () -> Unit,
    onQueryChanged: (String) -> Unit,
    onOpenBackup: () -> Unit,
    onOpenArchived: () -> Unit
) {
    val openTasks = remember(tasks) { tasks.count { !it.completed } }
    val timedTasks = remember(tasks) { tasks.count { !it.completed && (it.useAlarm || it.useCalendar) } }
    val completedTasks = remember(tasks) { tasks.count { it.completed } }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(searchVisible) {
        if (searchVisible) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AppCompassMark()
            Text(
                text = "DianMao",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HeaderActionButton(
                icon = if (searchVisible) Icons.Rounded.Close else Icons.Rounded.Search,
                contentDescription = if (searchVisible) "关闭搜索" else "搜索",
                onClick = onSearchToggle
            )
            HeaderActionButton(
                icon = Icons.Rounded.Archive,
                contentDescription = "归档事项",
                onClick = onOpenArchived
            )
            HeaderActionButton(
                icon = Icons.Rounded.Settings,
                contentDescription = "备份与恢复",
                onClick = onOpenBackup
            )
        }
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SummaryPill(value = openTasks.toString(), label = "未完成", color = MaterialTheme.colorScheme.primary)
            SummaryPill(value = timedTasks.toString(), label = "定时", color = MaterialTheme.colorScheme.secondary)
            SummaryPill(value = completedTasks.toString(), label = "已复盘", color = TaskPriority.NotImportantNotUrgent.color)
        }
        AnimatedVisibility(
            visible = searchVisible,
            enter = fadeIn(animationSpec = tween(200)) +
                slideInVertically(
                    animationSpec = tween(220),
                    initialOffsetY = { it / 4 }
                ),
            exit = fadeOut(animationSpec = tween(160)) +
                slideOutVertically(
                    animationSpec = tween(180),
                    targetOffsetY = { it / 4 }
                )
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RowShape,
                color = MaterialTheme.colorScheme.surface,
                border = quietBorder(0.10f)
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                    placeholder = {
                        Text(
                            text = "筛选当前视图",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = null
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { onQueryChanged("") }) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = "清除"
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Search),
                    colors = quietTextFieldColors(),
                    shape = RowShape
                )
            }
        }
    }
}

@Composable
internal fun TimeFocusLane(
    tasks: List<TaskUiModel>,
    onTaskSelected: (TaskUiModel) -> Unit
) {
    // Pure projection over the task list — remember it so scrolling/recomposing
    // the lane does not re-run the repeat-cycle math for every task.
    val focusItems = remember(tasks) { buildTimeFocusItems(tasks) }
    if (focusItems.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RowShape,
        border = quietBorder(0.14f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconTile(
                    icon = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                    iconSize = 15.dp
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(1.dp)
                ) {
                    Text(
                        text = "时间关注",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "过期、今天和最近安排",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${focusItems.size} 项",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.82f)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
internal fun TimeFocusRow(
    item: TimeFocusItem,
    onClick: () -> Unit
) {
    val color = when (item.kind) {
        TimeFocusKind.Overdue -> MaterialTheme.colorScheme.error
        TimeFocusKind.Today -> MaterialTheme.colorScheme.secondary
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
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            color = color.copy(alpha = 0.10f),
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
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = item.task.title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                item.dueLabel?.let {
                    CompactMeta(icon = Icons.Rounded.Schedule, text = it)
                }
                CompactMeta(
                    icon = Icons.Rounded.Inventory2,
                    text = item.task.quadrant.shortTitle,
                    tint = item.task.quadrant.color
                )
            }
        }
    }
}

@Composable
internal fun QuadrantBoard(
    tasks: List<TaskUiModel>,
    modifier: Modifier = Modifier,
    onSelected: (TaskPriority) -> Unit,
    emptyMessage: String? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
        shape = PanelShape,
        border = quietBorder(0.10f),
        shadowElevation = 0.dp
    ) {
        if (emptyMessage != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                IconTile(
                    icon = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                    iconSize = 18.dp
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = emptyMessage,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "换个关键词，或关闭搜索查看全部象限。",
                    modifier = Modifier.padding(top = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else Column {
            // Group once per task-list change instead of filtering four times
            // on every recomposition of the board.
            val groupedTasks = remember(tasks) {
                tasks.groupBy { it.quadrant }
            }
            Row(modifier = Modifier.weight(1f)) {
                QuadrantPane(
                    quadrant = TaskPriority.ImportantUrgent,
                    tasks = groupedTasks[TaskPriority.ImportantUrgent].orEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelected
                )
                BoardDivider(vertical = true)
                QuadrantPane(
                    quadrant = TaskPriority.ImportantNotUrgent,
                    tasks = groupedTasks[TaskPriority.ImportantNotUrgent].orEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelected
                )
            }
            BoardDivider(vertical = false)
            Row(modifier = Modifier.weight(1f)) {
                QuadrantPane(
                    quadrant = TaskPriority.NotImportantUrgent,
                    tasks = groupedTasks[TaskPriority.NotImportantUrgent].orEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelected
                )
                BoardDivider(vertical = true)
                QuadrantPane(
                    quadrant = TaskPriority.NotImportantNotUrgent,
                    tasks = groupedTasks[TaskPriority.NotImportantNotUrgent].orEmpty(),
                    modifier = Modifier.weight(1f),
                    onSelected = onSelected
                )
            }
        }
    }
}

@Composable
internal fun BoardDivider(vertical: Boolean) {
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
internal fun QuadrantPane(
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
            .background(paneColor, shape = PanelShape)
            .clickable { onSelected(quadrant) }
            .verticalScroll(rememberScrollState())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = quadrant.description,
                    style = MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Surface(
                color = quadrant.color.copy(alpha = 0.10f),
                contentColor = quadrant.color,
                shape = ChipShape
            ) {
                Text(
                    text = "$openCount 项",
                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelMedium,
                    maxLines = 1
                )
            }
        }
        if (visibleTasks.isEmpty()) {
            EmptyQuadrantHint(quadrant)
        } else {
            visibleTasks.forEach { task ->
                CompactQuadrantTask(task = task)
            }
            if (tasks.size > visibleTasks.size) {
                Text(
                    text = "另有 ${tasks.size - visibleTasks.size} 项，点开查看",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun EmptyQuadrantHint(quadrant: TaskPriority) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.50f), ChipShape)
            .border(1.dp, quadrant.color.copy(alpha = 0.08f), ChipShape)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "暂无${quadrant.shortTitle}事项",
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f)
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
internal fun CompactQuadrantTask(
    task: TaskUiModel
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (task.completed) 0.54f else 0.78f), ChipShape)
            .border(1.dp, task.quadrant.color.copy(alpha = if (task.completed) 0.06f else 0.10f), ChipShape)
            .padding(horizontal = 8.dp, vertical = 5.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.68f)
            )
        }
    }
}

/**
 * Light-weight home view switcher: 四象限 / 今天 / 近期 / 收集箱. Uses the same
 * segmented-row family as the form's priority selector so browsing never feels
 * like a second app. Quadrants stays the default and primary surface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HomeSmartViewSwitcher(
    selected: HomeSmartView,
    onSelect: (HomeSmartView) -> Unit
) {
    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
    ) {
        HomeSmartView.entries.forEachIndexed { index, view ->
            SegmentedButton(
                selected = view == selected,
                onClick = { onSelect(view) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = HomeSmartView.entries.size
                ),
                modifier = Modifier.weight(1f),
                label = {
                    Text(
                        text = view.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1
                    )
                },
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

/**
 * Task list behind the smart views: a quiet LazyColumn of TaskRows fed by the
 * pure smart-item projection, reusing every row action from the other lists.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun HomeSmartTaskList(
    items: List<HomeSmartTaskItem>,
    homeSmartView: HomeSmartView,
    repository: TaskRepository,
    onToggleComplete: (TaskUiModel) -> Unit,
    onCompleteSilently: (TaskUiModel) -> Unit,
    onEditTask: (TaskUiModel) -> Unit,
    onDeleteTask: (TaskUiModel) -> Unit,
    onEditNote: (TaskUiModel) -> Unit,
    onArchiveTask: (TaskUiModel) -> Unit,
    onViewDetail: (TaskUiModel) -> Unit,
    onAddTask: () -> Unit,
    emptyMessage: String?,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                IconTile(
                    icon = Icons.Rounded.Inventory2,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                    iconSize = 22.dp,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = emptyMessage ?: "这里还没有事项",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
                FilledTonalButton(
                    onClick = onAddTask,
                    shape = RowShape,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    InlineButtonIcon(
                        icon = Icons.Rounded.Add,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.10f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("新增事项")
                }
            }
        }
        return
    }
    // Group outside the LazyColumn: computing it inside the content lambda
    // would re-run the sort/partition on every scroll layout pass.
    val groups = remember(items, homeSmartView) {
        groupHomeSmartItems(items, homeSmartView)
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(top = 2.dp, bottom = 96.dp)
    ) {
        groups.forEach { group ->
            if (group.label.isNotEmpty()) {
                item(key = "group-${group.label}", contentType = "group-header") {
                    Text(
                        text = "${group.label} · ${group.items.size}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f)
                    )
                }
            }
            items(group.items, key = { it.task.id }, contentType = { "task-row" }) { item ->
                SwipeableTaskRow(
                    task = item.task,
                    repository = repository,
                    onToggleComplete = onToggleComplete,
                    onCompleteSilently = onCompleteSilently,
                    onDeleteTask = onDeleteTask
                ) {
                    TaskRow(
                        task = item.task,
                        repository = repository,
                        dueLabelOverride = item.dueLabel,
                        onToggleComplete = onToggleComplete,
                        onEditTask = onEditTask,
                        onDeleteTask = onDeleteTask,
                        onEditNote = onEditNote,
                        onArchiveTask = onArchiveTask,
                        onViewDetail = onViewDetail,
                        showQuadrantMeta = true,
                        modifier = with(this@items) { Modifier.animateItemPlacement() }
                    )
                }
            }
        }
    }
}

/**
 * Row-level swipe gestures, mainstream to-do style: swipe right to complete
 * silently (a 写心得 action on the top notice can add a note afterwards) or
 * un-complete, swipe left to request deletion (which still runs through the
 * confirm dialog + undo chain). The wrapped [content] stays interactive —
 * drags only start from the horizontal swipe, leaving long-press drag
 * reorder untouched.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SwipeableTaskRow(
    task: TaskUiModel,
    repository: TaskRepository,
    onToggleComplete: (TaskUiModel) -> Unit,
    onCompleteSilently: (TaskUiModel) -> Unit,
    onDeleteTask: (TaskUiModel) -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    var canComplete by remember(task.id, task.lastCompletedAtMillis, task.completed) {
        mutableStateOf(true)
    }
    LaunchedEffect(task.id, task.lastCompletedAtMillis, task.completed) {
        canComplete = repository.canCompleteInCurrentCycle(task)
    }
    val haptics = LocalHapticFeedback.current
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            // Always settle back: the row never stays dismissed. Actions fire
            // here and the list updates through the data flow, so a completed
            // task simply leaves the smart view and a cancelled delete leaves
            // the row untouched — no stuck states.
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    if (canComplete || task.completed) {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (task.completed || task.repeatRule != RepeatRule.None) {
                            // Un-completing a recurring task keeps its confirm
                            // dialog; everything else completes silently.
                            onToggleComplete(task)
                        } else {
                            onCompleteSilently(task)
                        }
                    }
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDeleteTask(task)
                    false
                }
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.4f }
    )
    // Reset the gesture state whenever the task identity or completion state
    // changes, so a recycled row never inherits a stale swipe offset.
    key(task.id, task.completed) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = canComplete || task.completed,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val settled = dismissState.targetValue == SwipeToDismissBoxValue.Settled
                val tint = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> task.quadrant.color
                    else -> MaterialTheme.colorScheme.error
                }
                val label = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        if (task.completed) "标为未完成" else "完成"
                    else -> "删除"
                }
                val icon = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd ->
                        if (task.completed) Icons.Rounded.Replay else Icons.Rounded.DoneAll
                    else -> Icons.Rounded.Delete
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(tint.copy(alpha = if (settled) 0.08f else 0.16f), RowShape)
                        .padding(horizontal = 20.dp),
                    contentAlignment = if (direction == SwipeToDismissBoxValue.StartToEnd) {
                        Alignment.CenterStart
                    } else {
                        Alignment.CenterEnd
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = tint
                        )
                    }
                }
            },
            content = content
        )
    }
}
