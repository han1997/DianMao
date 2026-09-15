package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskUiModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private val DayCellShape = RoundedCornerShape(10.dp)

/**
 * Full-screen calendar surface: a Monday-first month grid with per-day
 * quadrant dots, a selected-day task list below it, and a 记一笔 shortcut
 * that opens the add sheet with the selected day pre-filled as the due date.
 * Grouping and grid layout are pure projections over active tasks (see
 * [buildCalendarDayMap] / [buildMonthGrid]) — no DB access here.
 */
@Composable
internal fun CalendarMonthScreen(
    visible: Boolean,
    tasks: List<TaskUiModel>,
    nowMillis: Long,
    zoneId: ZoneId,
    onBack: () -> Unit,
    onTaskSelected: (TaskUiModel) -> Unit,
    onQuickAdd: (LocalDate) -> Unit
) {
    if (!visible) return

    val today = remember(nowMillis, zoneId) {
        Instant.ofEpochMilli(nowMillis).atZone(zoneId).toLocalDate()
    }
    var monthStartEpochDay by rememberSaveable {
        mutableStateOf(today.withDayOfMonth(1).toEpochDay())
    }
    var selectedEpochDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    val month = remember(monthStartEpochDay) {
        YearMonth.from(LocalDate.ofEpochDay(monthStartEpochDay))
    }
    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val dayMap = remember(tasks, nowMillis, zoneId) {
        buildCalendarDayMap(tasks, nowMillis, zoneId)
    }
    val cells = remember(month) { buildMonthGrid(month) }
    val dayTasks = remember(dayMap, selectedDate) { dayMap[selectedDate].orEmpty() }

    FullScreenScaffold(
        title = "日历",
        subtitle = "按月浏览事项",
        leadingIcon = Icons.Rounded.CalendarMonth,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 12.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { monthStartEpochDay = month.minusMonths(1).atDay(1).toEpochDay() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "上个月",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${month.year}年${month.monthValue}月",
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = { monthStartEpochDay = month.plusMonths(1).atDay(1).toEpochDay() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "下个月",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("一", "二", "三", "四", "五", "六", "日").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                    )
                }
            }

            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { cell ->
                        CalendarDayCell(
                            cell = cell,
                            isToday = cell.date == today,
                            isSelected = cell.date == selectedDate,
                            tasks = dayMap[cell.date].orEmpty(),
                            onClick = {
                                selectedEpochDay = cell.date.toEpochDay()
                                if (!cell.inMonth) {
                                    monthStartEpochDay = cell.date.withDayOfMonth(1).toEpochDay()
                                }
                            }
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RowShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                border = quietBorder(0.10f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日 · " +
                                "${dayTasks.count { !it.task.completed }} 项待办",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.titleSmall.copy(letterSpacing = 0.sp),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(onClick = { onQuickAdd(selectedDate) }) {
                            Icon(
                                Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "记一笔",
                                modifier = Modifier.padding(start = 4.dp),
                                style = MaterialTheme.typography.labelLarge.copy(letterSpacing = 0.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (dayTasks.isEmpty()) {
                        Text(
                            text = "当天没有事项",
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                        )
                    } else {
                        dayTasks.forEach { item ->
                            CalendarTaskRow(item = item, onClick = { onTaskSelected(item.task) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    cell: MonthDayCell,
    isToday: Boolean,
    isSelected: Boolean,
    tasks: List<HomeSmartTaskItem>,
    onClick: () -> Unit
) {
    val dotColors = remember(tasks) { tasks.map { it.task.quadrant.color }.distinct() }
    val visibleColors = dotColors.take(3)
    val overflow = dotColors.size - visibleColors.size
    val numberColor = when {
        !cell.inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.muted)
        isToday -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .padding(1.dp)
            .clip(DayCellShape)
            .background(
                if (isSelected) {
                    MaterialTheme.colorScheme.primary.copy(alpha = DianMaoAlpha.subtle)
                } else {
                    androidx.compose.ui.graphics.Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = cell.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = if (isToday || isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    letterSpacing = 0.sp
                ),
                color = numberColor
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                visibleColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(color, CircleShape)
                    )
                }
                if (overflow > 0) {
                    Text(
                        text = "+$overflow",
                        style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarTaskRow(item: HomeSmartTaskItem, onClick: () -> Unit) {
    val task = item.task
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface,
        border = quietBorder(0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ColorDot(
                color = task.quadrant.color.copy(alpha = if (task.completed) 0.4f else 1f),
                size = 10.dp
            )
            Text(
                text = task.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                color = if (task.completed) {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            item.dueLabel?.let { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
