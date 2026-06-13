package `fun`.han1997.dianmao

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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

private enum class Quadrant(
    val title: String,
    val shortTitle: String,
    val description: String,
    val color: Color
) {
    ImportantUrgent("重要且紧急", "重急", "今天必须推进", Color(0xFFB94732)),
    ImportantNotUrgent("重要不紧急", "重要", "长期价值任务", Color(0xFF7C5E16)),
    NotImportantUrgent("不重要但紧急", "紧急", "有时间压力", Color(0xFF2F6FBB)),
    NotImportantNotUrgent("不重要不紧急", "低压", "可延后处理", Color(0xFF58706D))
}

private data class TaskItem(
    val id: Int,
    val title: String,
    val quadrant: Quadrant,
    val dueLabel: String? = null,
    val useCalendar: Boolean = false,
    val useAlarm: Boolean = false,
    val completed: Boolean = false,
    val completionNote: String? = null
)

private val seedTasks = listOf(
    TaskItem(
        id = 1,
        title = "梳理本周必须交付的工作",
        quadrant = Quadrant.ImportantUrgent,
        dueLabel = "今天 18:00",
        useCalendar = true,
        useAlarm = true
    ),
    TaskItem(
        id = 2,
        title = "设计四象限任务模型",
        quadrant = Quadrant.ImportantNotUrgent,
        dueLabel = "长期",
        useCalendar = false,
        useAlarm = false
    ),
    TaskItem(
        id = 3,
        title = "确认明早会议提醒",
        quadrant = Quadrant.NotImportantUrgent,
        dueLabel = "明天 09:30",
        useCalendar = true,
        useAlarm = true
    ),
    TaskItem(
        id = 4,
        title = "整理完成任务心得",
        quadrant = Quadrant.NotImportantNotUrgent,
        dueLabel = null,
        useCalendar = false,
        useAlarm = false,
        completed = true,
        completionNote = "完成后记录判断依据，方便以后复盘同类任务。"
    )
)

@Composable
private fun DianMaoTheme(content: @Composable () -> Unit) {
    val colorScheme = androidx.compose.material3.lightColorScheme(
        primary = Color(0xFFB94732),
        onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDAD2),
        onPrimaryContainer = Color(0xFF3D0700),
        secondary = Color(0xFF2F6FBB),
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFD6E3FF),
        onSecondaryContainer = Color(0xFF001B3E),
        background = Color(0xFFFCFCFC),
        onBackground = Color(0xFF1F1F1F),
        surface = Color(0xFFFFFFFF),
        onSurface = Color(0xFF1F1F1F),
        surfaceVariant = Color(0xFFF0F1F1),
        onSurfaceVariant = Color(0xFF5F6368),
        outline = Color(0xFFD7D9DC)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 28.sp,
                lineHeight = 34.sp,
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
    val tasks = remember { mutableStateListOf<TaskItem>().apply { addAll(seedTasks) } }
    var selectedQuadrant by rememberSaveable { mutableStateOf<Quadrant?>(null) }
    var showAddSheet by rememberSaveable { mutableStateOf(false) }
    var taskForNote by remember { mutableStateOf<TaskItem?>(null) }

    val visibleTasks = remember(tasks.toList(), selectedQuadrant) {
        if (selectedQuadrant == null) tasks else tasks.filter { it.quadrant == selectedQuadrant }
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        AppHeader(tasks = tasks)
                    }
                    item {
                        QuadrantFilter(
                            selected = selectedQuadrant,
                            onSelected = { selectedQuadrant = it }
                        )
                    }
                    item {
                        TodayFocus(tasks = tasks.filter { !it.completed })
                    }
                    item {
                        SectionHeader(
                            title = selectedQuadrant?.title ?: "全部事项",
                            subtitle = "${visibleTasks.size} 个事项，按优先级和时间一起管理"
                        )
                    }
                    items(visibleTasks, key = { it.id }) { task ->
                        TaskRow(
                            task = task,
                            onToggleComplete = { target ->
                                val index = tasks.indexOfFirst { it.id == target.id }
                                if (index >= 0) {
                                    if (target.completed) {
                                        tasks[index] = target.copy(completed = false, completionNote = null)
                                    } else {
                                        taskForNote = target
                                    }
                                }
                            },
                            onEditNote = { taskForNote = it }
                        )
                    }
                }

                AddTaskPanel(
                    visible = showAddSheet,
                    onDismiss = { showAddSheet = false },
                    onCreate = { title, quadrant, dueLabel, useCalendar, useAlarm ->
                        val nextId = (tasks.maxOfOrNull { it.id } ?: 0) + 1
                        tasks.add(
                            0,
                            TaskItem(
                                id = nextId,
                                title = title,
                                quadrant = quadrant,
                                dueLabel = dueLabel,
                                useCalendar = useCalendar,
                                useAlarm = useAlarm
                            )
                        )
                        showAddSheet = false
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
                val index = tasks.indexOfFirst { it.id == task.id }
                if (index >= 0) {
                    tasks[index] = task.copy(completed = true, completionNote = note.trim().ifBlank { null })
                }
                taskForNote = null
            }
        )
    }
}

@Composable
private fun AppHeader(tasks: List<TaskItem>) {
    val openTasks = tasks.count { !it.completed }
    val timedTasks = tasks.count { !it.completed && (it.useAlarm || it.useCalendar) }
    val today = LocalDate.now().format(DateTimeFormatter.ofPattern("M月d日"))

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "DianMao",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "$today · $openTasks 个未完成事项 · $timedTasks 个有时间提醒",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "先写下来，再决定象限、时间和提醒。完成后留下备注，给之后的判断复盘。",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuadrantFilter(
    selected: Quadrant?,
    onSelected: (Quadrant?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "四象限", subtitle = "当天任务和长期任务使用同一套优先级模型")
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selected == null,
                onClick = { onSelected(null) },
                label = { Text("全部") }
            )
            Quadrant.entries.forEach { quadrant ->
                FilterChip(
                    selected = selected == quadrant,
                    onClick = { onSelected(quadrant) },
                    label = { Text(quadrant.title) },
                    leadingIcon = { ColorDot(quadrant.color) }
                )
            }
        }
    }
}

@Composable
private fun TodayFocus(tasks: List<TaskItem>) {
    val urgent = tasks.count { it.quadrant == Quadrant.ImportantUrgent || it.useAlarm }
    val longTerm = tasks.count { it.quadrant == Quadrant.ImportantNotUrgent }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = "今天先看这些",
                    style = MaterialTheme.typography.titleMedium
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                FocusStat(
                    value = urgent.toString(),
                    label = "紧急/响铃",
                    color = MaterialTheme.colorScheme.primary
                )
                FocusStat(
                    value = longTerm.toString(),
                    label = "长期重要",
                    color = Color(0xFF7C5E16)
                )
            }
        }
    }
}

@Composable
private fun FocusStat(value: String, label: String, color: Color) {
    Column(
        modifier = Modifier
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .width(112.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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
    task: TaskItem,
    onToggleComplete: (TaskItem) -> Unit,
    onEditNote: (TaskItem) -> Unit
) {
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
        shape = RoundedCornerShape(18.dp),
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
            IconButton(onClick = { onEditNote(task) }) {
                Icon(
                    imageVector = if (task.completed) Icons.Rounded.EditNote else Icons.Rounded.DoneAll,
                    contentDescription = if (task.completed) "编辑完成备注" else "完成并写备注",
                    tint = MaterialTheme.colorScheme.primary
                )
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
    onDismiss: () -> Unit,
    onCreate: (String, Quadrant, String?, Boolean, Boolean) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var quadrant by rememberSaveable { mutableStateOf(Quadrant.ImportantUrgent) }
    var dueLabel by rememberSaveable { mutableStateOf("") }
    var useCalendar by rememberSaveable { mutableStateOf(false) }
    var useAlarm by rememberSaveable { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(150)) + slideInVertically(tween(220)) { it / 4 },
        exit = fadeOut(tween(120)) + slideOutVertically(tween(180)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.18f))
                .clickable(onClick = onDismiss)
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                            Text("新增事项", style = MaterialTheme.typography.titleLarge)
                            Text(
                                "先记录，再补象限和时间",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "关闭新增面板")
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
                            Quadrant.entries.forEach { item ->
                                FilterChip(
                                    selected = item == quadrant,
                                    onClick = { quadrant = item },
                                    label = { Text(item.shortTitle) },
                                    leadingIcon = { ColorDot(item.color) }
                                )
                            }
                        }
                    }
                    OutlinedTextField(
                        value = dueLabel,
                        onValueChange = { dueLabel = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("时间") },
                        placeholder = { Text("今天 18:00 / 明天 09:30 / 长期") },
                        singleLine = true
                    )
                    ToggleRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = "同步到系统日历",
                        subtitle = "当前版本先记录意图，后续接入 Calendar Provider",
                        checked = useCalendar,
                        onCheckedChange = { useCalendar = it }
                    )
                    ToggleRow(
                        icon = Icons.Rounded.Alarm,
                        title = "设置响铃提醒",
                        subtitle = "当前版本先记录意图，后续接入 AlarmManager",
                        checked = useAlarm,
                        onCheckedChange = { useAlarm = it }
                    )
                    Button(
                        onClick = {
                            val trimmed = title.trim()
                            if (trimmed.isNotEmpty()) {
                                onCreate(trimmed, quadrant, dueLabel.trim().ifBlank { null }, useCalendar, useAlarm)
                                title = ""
                                dueLabel = ""
                                useCalendar = false
                                useAlarm = false
                                quadrant = Quadrant.ImportantUrgent
                            }
                        },
                        enabled = title.isNotBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(vertical = 14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("保存事项")
                    }
                }
            }
        }
    }
}

@Composable
private fun ToggleRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun CompletionNoteDialog(
    task: TaskItem,
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
