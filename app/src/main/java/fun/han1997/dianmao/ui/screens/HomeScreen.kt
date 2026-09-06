package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.data.TaskRepository
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.AppCompassMark
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.CompactMeta
import `fun`.han1997.dianmao.CompletionMark
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val completedTasks = tasks.count { it.completed }
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
    val focusItems = buildTimeFocusItems(tasks)
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
