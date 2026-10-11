package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.FormSettingBlock
import `fun`.han1997.dianmao.FullScreenScaffold
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.InlineStatDot
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Full-screen review surface: completion stats (7/30-day totals, the last 7
 * days' distribution, quadrant share) plus the recent completion notes.
 */
@Composable
internal fun ReviewScreen(
    visible: Boolean,
    events: List<ReviewCompletionEvent>,
    stats: ReviewStats,
    onBack: () -> Unit
) {
    if (!visible) return
    FullScreenScaffold(
        title = "复盘",
        subtitle = "完成回顾与心得",
        leadingIcon = Icons.Rounded.QueryStats,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 16.dp, bottom = 14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            FormSettingBlock(
                title = "完成概览",
                subtitle = "按本地时间统计完成次数",
                icon = Icons.Rounded.QueryStats
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InlineStatDot(
                        count = stats.last7DaysCount,
                        label = "近 7 天",
                        color = MaterialTheme.colorScheme.primary
                    )
                    InlineStatDot(
                        count = stats.last30DaysCount,
                        label = "近 30 天",
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            FormSettingBlock(
                title = "近 7 天每日分布",
                icon = Icons.Rounded.QueryStats
            ) {
                val maxCount = stats.dailyCounts.maxOfOrNull { it.count } ?: 0
                stats.dailyCounts.forEach { day ->
                    ReviewDayBarRow(
                        day = day,
                        maxCount = maxCount,
                        isToday = day == stats.dailyCounts.last()
                    )
                }
            }

            FormSettingBlock(
                title = "四象限占比",
                subtitle = "近 30 天各象限完成次数",
                icon = Icons.Rounded.QueryStats
            ) {
                TaskPriority.entries.forEach { quadrant ->
                    val count = stats.quadrantCounts[quadrant] ?: 0
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ColorDot(quadrant.color)
                        Text(
                            text = quadrant.shortTitle,
                            style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Box(modifier = Modifier.weight(1f))
                        Text(
                            text = "$count 次",
                            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
                        )
                    }
                }
            }

            FormSettingBlock(
                title = "最近完成",
                subtitle = "最新 ${minOf(events.size, REVIEW_EVENTS_LIMIT)} 条记录",
                icon = Icons.Rounded.QueryStats
            ) {
                if (events.isEmpty()) {
                    Text(
                        text = "暂无完成记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                    )
                } else {
                    events.take(REVIEW_EVENTS_LIMIT).forEach { event ->
                        ReviewEventRow(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewDayBarRow(
    day: ReviewDayCount,
    maxCount: Int,
    isToday: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = if (isToday) "今天" else "${day.date.monthValue}/${day.date.dayOfMonth}",
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
            color = if (isToday) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            },
            modifier = Modifier.widthIn(min = 44.dp)
        )
        val fraction = if (maxCount <= 0) 0f else day.count.toFloat() / maxCount
        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
        ) {
            if (day.count > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.55f))
                )
            }
        }
        Text(
            text = "${day.count}",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(min = 20.dp)
        )
    }
}

@Composable
private fun ReviewEventRow(event: ReviewCompletionEvent) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = quietBorder(0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorDot(event.quadrant.color)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = DateTimeFormatters.formatTimestamp(event.completedAtMillis),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (!event.note.isNullOrBlank()) {
                    Text(
                        text = event.note,
                        style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
