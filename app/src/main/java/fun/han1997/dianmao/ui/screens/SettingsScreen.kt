package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.appearance.ThemeMode
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val GroupShape = RoundedCornerShape(16.dp)
private val BrandMarkShape = RoundedCornerShape(17.dp)

/**
 * Settings level 1 — the prototype's two-level settings hub. Groups are
 * separated by small section labels; each row is a 12dp/14dp padded button with
 * a leading 30dp icon tile, a two-line title/subtitle and either a chevron or a
 * value tag.
 */
@Composable
internal fun SettingsScreen(
    themeMode: ThemeMode,
    backupConnected: Boolean,
    backupHost: String?,
    archivedCount: Int,
    completedCount: Int,
    onBack: () -> Unit,
    onOpenAppearance: () -> Unit,
    onOpenWidget: () -> Unit,
    onOpenBackup: () -> Unit,
    onOpenArchive: () -> Unit,
    onOpenReview: () -> Unit
) {
    FullScreenScaffold(
        title = "设置",
        subtitle = "偏好与数据",
        leadingIcon = DianMaoIcons.Settings,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 6.dp, bottom = 24.dp)
        ) {
            SettingsHero()

            SettingsGroupLabel("外观")
            SettingsGroup {
                SettingsRow(
                    icon = DianMaoIcons.Moon,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "主题",
                    subtitle = "浅色、深色或跟随系统",
                    onClick = onOpenAppearance,
                    trailing = { SettingsTag(themeModeLabel(themeMode)) }
                )
                SettingsRowDivider()
                SettingsRow(
                    icon = DianMaoIcons.Inventory,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "小组件与通知",
                    subtitle = "桌面小组件与到点提醒样式",
                    onClick = onOpenWidget
                )
            }

            SettingsGroupLabel("数据")
            SettingsGroup {
                SettingsRow(
                    icon = DianMaoIcons.CloudUp,
                    iconTint = MaterialTheme.colorScheme.secondary,
                    title = "备份与恢复",
                    subtitle = if (backupConnected && backupHost != null) {
                        "$backupHost · 已连接"
                    } else {
                        "本地导出与 WebDAV 同步"
                    },
                    onClick = onOpenBackup,
                    trailing = {
                        SettingsTag(
                            label = if (backupConnected) "已连接" else "未配置",
                            warn = !backupConnected
                        )
                    }
                )
                SettingsRowDivider()
                SettingsRow(
                    icon = DianMaoIcons.Archive,
                    iconTint = androidx.compose.ui.graphics.Color(0xFF4E6D68),
                    title = "归档事项",
                    subtitle = "$archivedCount 项已归档",
                    onClick = onOpenArchive
                )
                SettingsRowDivider()
                SettingsRow(
                    icon = DianMaoIcons.Chart,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "复盘",
                    subtitle = "$completedCount 项已完成可复盘",
                    onClick = onOpenReview
                )
            }

            SettingsGroupLabel("关于")
            SettingsGroup {
                SettingsRow(
                    icon = DianMaoIcons.Compass,
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "关于 DianMao",
                    subtitle = "版本、设计语言与隐私说明",
                    onClick = {}
                )
            }

            Text(
                text = "所有数据与偏好仅保存在本机;备份由你自行指定目标。",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            )
        }
    }
}

@Composable
private fun SettingsHero() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 2.dp, end = 2.dp, top = 6.dp, bottom = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(BrandMarkShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "D",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Column {
            Text(
                text = "DianMao",
                style = MaterialTheme.typography.titleLarge.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "v${BuildConfig.VERSION_NAME} · The Day Compass",
                modifier = Modifier.padding(top = 2.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            )
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 6.dp),
        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun SettingsGroup(content: @Composable () -> Unit) {
    androidx.compose.material3.Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 18.dp),
        shape = GroupShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
        border = quietBorder(0.12f)
    ) {
        Column { content() }
    }
}

@Composable
private fun SettingsRowDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    )
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(
            icon = icon,
            contentDescription = null,
            tint = iconTint,
            containerColor = iconTint.copy(alpha = 0.10f)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (trailing != null) {
            trailing()
        } else {
            Box(
                modifier = Modifier.size(18.dp),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = DianMaoIcons.ChevRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.muted),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingsTag(label: String, warn: Boolean = false) {
    val bg = if (warn) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f)
    }
    val fg = if (warn) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
            color = fg
        )
    }
}

internal fun themeModeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.System -> "跟随系统"
    ThemeMode.Light -> "浅色"
    ThemeMode.Dark -> "深色"
}

/**
 * Settings level 2 — appearance. A radio list for the three modes plus the
 * prototype's two-tone preview card and explanatory hints.
 */
@Composable
internal fun AppearanceScreen(
    themeMode: ThemeMode,
    isSystemDark: Boolean,
    onBack: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    FullScreenScaffold(
        title = "主题",
        subtitle = "浅色、深色或跟随系统",
        leadingIcon = DianMaoIcons.Moon,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        onBack = onBack
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(top = 6.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = GroupShape,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                border = quietBorder(0.12f)
            ) {
                Column {
                    AppearanceChoice(
                        mode = ThemeMode.System,
                        selected = themeMode == ThemeMode.System,
                        icon = DianMaoIcons.Moon,
                        title = "跟随系统",
                        subtitle = "随系统设置自动切换",
                        onSelect = onSelect
                    )
                    SettingsRowDivider()
                    AppearanceChoice(
                        mode = ThemeMode.Light,
                        selected = themeMode == ThemeMode.Light,
                        icon = DianMaoIcons.Sun,
                        title = "浅色",
                        subtitle = "始终使用暖纸亮色主题",
                        onSelect = onSelect
                    )
                    SettingsRowDivider()
                    AppearanceChoice(
                        mode = ThemeMode.Dark,
                        selected = themeMode == ThemeMode.Dark,
                        icon = DianMaoIcons.Moon,
                        title = "深色",
                        subtitle = "始终使用深暖色主题",
                        onSelect = onSelect
                    )
                }
            }

            ThemePreview()

            Text(
                text = if (themeMode == ThemeMode.System) {
                    "当前跟随系统:${if (isSystemDark) "深色" else "浅色"},系统主题变化时自动生效。"
                } else {
                    "已固定为${themeModeLabel(themeMode)}主题。切回跟随系统即可自动跟随。"
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            )
            Text(
                text = "主题偏好保存在本机,不随备份上传。",
                modifier = Modifier.padding(horizontal = 4.dp),
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
            )
        }
    }
}

@Composable
private fun AppearanceChoice(
    mode: ThemeMode,
    selected: Boolean,
    icon: ImageVector,
    title: String,
    subtitle: String,
    onSelect: (ThemeMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(role = Role.RadioButton) { onSelect(mode) }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconTile(
            icon = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(letterSpacing = 0.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
            )
        }
        // Prototype .radio: 20dp; unselected shows a 2dp outline ring,
        // selected fills primary with a 10dp onPrimary core.
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .then(
                    if (selected) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = CircleShape
                        )
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onPrimary)
                )
            }
        }
    }
}

@Composable
private fun ThemePreview() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ThemePreviewCard(dark = true, modifier = Modifier.weight(1f))
        ThemePreviewCard(dark = false, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ThemePreviewCard(dark: Boolean, modifier: Modifier = Modifier) {
    val bg = if (dark) Color(0xFF201C19) else Color(0xFFFFFCF9)
    val swatch = if (dark) Color(0xFFE0806C) else Color(0xFFA84534)
    val line = if (dark) Color(0xFFEAE5E0).copy(alpha = 0.16f) else Color(0xFF1E211F).copy(alpha = 0.16f)
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(swatch)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(line)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(8.dp)
                .clip(CircleShape)
                .background(line)
        )
    }
}