package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskUiModel
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Settings level 2 — the prototype's "小组件与通知" chapter: a wide widget
 * preview (up to 8 rows), a narrow widget (up to 4 rows) and two notification
 * previews (normal / recurring). These are static previews of the real Glance
 * widget and reminder-notification layouts.
 */
@Composable
internal fun WidgetNotificationsScreen(
    sampleTasks: List<TaskUiModel>,
    onBack: () -> Unit,
    onOpenNotificationSettings: () -> Unit
) {
    FullScreenScaffold(
        title = "小组件与通知",
        subtitle = "桌面小组件与到点提醒通知",
        leadingIcon = DianMaoIcons.Inventory,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            WidgetSectionLabel("宽小组件 · 最多 8 条")
            WidgetPreview(tasks = sampleTasks.take(8), limit = 8)

            WidgetSectionLabel("窄小组件 · 最多 4 条")
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                WidgetPreview(
                    tasks = sampleTasks.take(4),
                    limit = 4,
                    modifier = Modifier.width(200.dp)
                )
            }

            WidgetSectionLabel("到点提醒通知")
            NotificationPreview(
                title = sampleTasks.firstOrNull()?.title ?: "整理季度 OKR 复盘草稿",
                recurring = false
            )

            WidgetSectionLabel("重复事项提醒 · 完成文案变为「记一次完成」")
            NotificationPreview(
                title = sampleTasks.firstOrNull()?.title ?: "整理季度 OKR 复盘草稿",
                recurring = true
            )

            WidgetSectionLabel("系统")
            SettingsGroup {
                SettingsRow(
                    icon = DianMaoIcons.Bell,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "通知设置",
                    subtitle = "打开系统的应用通知设置",
                    onClick = onOpenNotificationSettings
                )
            }

            Text(
                text = "小组件与通知均为演示:勾选/按钮不会真正修改数据或发送通知。",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            )
        }
    }
}

@Composable
private fun WidgetSectionLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun WidgetPreview(
    tasks: List<TaskUiModel>,
    limit: Int,
    modifier: Modifier = Modifier
) {
    // Prototype .wg is a fixed warm-paper widget surface that does not follow
    // the app color scheme (it mirrors the home-screen widget), so use the
    // literal light values in both themes and lighten them in dark.
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bg = if (isDark) Color(0xFF2A221C) else Color(0xFFF6F1E9)
    val fg = if (isDark) Color(0xFFEDE6DA) else Color(0xFF2B211B)
    val accent = if (isDark) Color(0xFFD98A76) else Color(0xFFA94332)
    val muted = if (isDark) Color(0xFFA89A8D) else Color(0xFF6B5F55)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(
                width = 1.dp,
                color = Color.Black.copy(alpha = 0.06f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "DianMao · 今天",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = fg
            )
            Text(
                text = "${minOf(limit, tasks.size)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = accent
            )
        }
        if (tasks.isEmpty()) {
            Text(
                text = "今天没有待办",
                modifier = Modifier.padding(vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = muted
            )
        } else {
            tasks.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = DianMaoIcons.CheckCircle,
                        contentDescription = "完成「${task.title}」",
                        tint = accent,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = task.title,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            letterSpacing = 0.sp
                        ),
                        color = fg,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    task.dueLabel?.let { label ->
                        Text(
                            text = label.substringAfterLast(' '),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Normal,
                                letterSpacing = 0.sp
                            ),
                            color = accent,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        Text(
            text = "+ 记一笔",
            modifier = Modifier.padding(top = 6.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            color = accent
        )
    }
}

@Composable
private fun NotificationPreview(title: String, recurring: Boolean) {
    androidx.compose.material3.Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = quietBorder(0.12f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = DianMaoIcons.Bell,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Text(
                    text = "DianMao 提醒",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "现在",
                    style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NotificationAction(
                    icon = DianMaoIcons.DoneAll,
                    label = if (recurring) "记一次完成" else "完成",
                    modifier = Modifier.weight(1f)
                )
                NotificationAction(
                    icon = DianMaoIcons.Schedule,
                    label = "稍后提醒",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun NotificationAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f))
            .clickable(role = Role.Button, onClick = {})
            .padding(horizontal = 8.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = label,
            modifier = Modifier.padding(start = 6.dp),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.primary
        )
    }
}