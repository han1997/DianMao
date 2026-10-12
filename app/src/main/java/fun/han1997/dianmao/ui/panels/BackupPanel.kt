package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons

import `fun`.han1997.dianmao.backup.AutoBackupSettings
import `fun`.han1997.dianmao.backup.WebDavSettings
import `fun`.han1997.dianmao.backup.autoBackupStatusText
import `fun`.han1997.dianmao.backup.resolveBackupFileUrl
import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.FormSettingBlock
import `fun`.han1997.dianmao.FullScreenScaffold
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.webDavHost
import `fun`.han1997.dianmao.quietBorder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun BackupPanel(
    visible: Boolean,
    initialSettings: WebDavSettings,
    autoBackupSettings: AutoBackupSettings,
    onDismiss: () -> Unit,
    onLocalExport: () -> Unit,
    onLocalImport: () -> Unit,
    onSaveWebDav: (WebDavSettings) -> Unit,
    onUploadWebDav: (WebDavSettings) -> Unit,
    onRestoreWebDav: (WebDavSettings) -> Unit,
    onToggleAutoBackup: (Boolean) -> Unit,
    syncInProgress: Boolean = false
) {
    var url by rememberSaveable { mutableStateOf(initialSettings.url) }
    var username by rememberSaveable { mutableStateOf(initialSettings.username) }
    var password by rememberSaveable { mutableStateOf(initialSettings.password) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    val webDavSettings = WebDavSettings(url = url, username = username, password = password)
    val webDavReady = url.isNotBlank()
    val backupScrollState = rememberScrollState()

    LaunchedEffect(visible, initialSettings) {
        if (visible) {
            url = initialSettings.url
            username = initialSettings.username
            password = initialSettings.password
            passwordVisible = false
        }
    }

    if (visible) {
        FullScreenScaffold(
            title = "备份与恢复",
            subtitle = "本地文件与 WebDAV 同步",
            leadingIcon = DianMaoIcons.Settings,
            leadingIconTint = MaterialTheme.colorScheme.primary,
            onBack = onDismiss,
            actions = {
                HeaderActionButton(
                    icon = DianMaoIcons.Close,
                    contentDescription = "关闭备份页面",
                    onClick = onDismiss
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(backupScrollState),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Sync status header card: parsed host + connection state chip
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RowShape,
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f),
                        border = quietBorder(0.14f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            IconTile(
                                icon = DianMaoIcons.CloudUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.10f),
                                iconSize = 18.dp
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = webDavHost(url) ?: "未配置云端同步",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = 0.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = if (webDavReady) {
                                        "WebDAV 云端备份目标 · " + resolveBackupFileUrl(url).substringAfterLast('/')
                                    } else {
                                        "配置下方地址后可上传与恢复"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = if (webDavReady) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = DianMaoAlpha.muted)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                                    },
                                    contentColor = if (webDavReady) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
                                    },
                                    shape = ChipShape
                                ) {
                                    Text(
                                        text = if (webDavReady) "已连接" else "未配置",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Medium,
                                            letterSpacing = 0.sp
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    FormSettingBlock(title = "本地备份", subtitle = "导出当前数据，或从文件恢复") {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                if (webDavReady) {
                                    // Cloud is configured: the single filled-primary slot
                                    // belongs to 上传备份 in 云端同步, so local export is outlined.
                                    OutlinedButton(
                                        onClick = onLocalExport,
                                        modifier = Modifier.weight(1f),
                                        shape = RowShape,
                                        contentPadding = PaddingValues(vertical = 11.dp)
                                    ) {
                                        InlineButtonIcon(
                                            icon = DianMaoIcons.FileUp,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("导出本地")
                                    }
                                } else {
                                    // Cloud unconfigured: local export takes the primary slot.
                                    Button(
                                        onClick = onLocalExport,
                                        modifier = Modifier.weight(1f),
                                        shape = RowShape,
                                        contentPadding = PaddingValues(vertical = 13.dp)
                                    ) {
                                        InlineButtonIcon(
                                            icon = DianMaoIcons.FileUp,
                                            tint = MaterialTheme.colorScheme.onPrimary,
                                            containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                                        )
                                        Spacer(Modifier.width(6.dp))
                                        Text("导出本地")
                                    }
                                }
                                OutlinedButton(
                                    onClick = onLocalImport,
                                    modifier = Modifier.weight(1f),
                                    shape = RowShape,
                                    contentPadding = PaddingValues(vertical = 11.dp)
                                ) {
                                    InlineButtonIcon(
                                        icon = DianMaoIcons.FileDown,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("本地恢复")
                                }
                            }
                        }

                         FormSettingBlock(title = "WebDAV 配置", subtitle = "填目录地址自动建目录备份;以 .json 结尾则视为文件") {
                             QuietField(
                                 value = url,
                                 onValueChange = { url = it },
                                 placeholder = "https://dav.example.com/dianmao/ 或 http://192.168.x.x/dav/",
                                 label = "备份目录 URL",
                                 supportingText = "备份写入目录内 dianmao-backup-latest.json;目录不存在会自动创建"
                             )
                            QuietField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = "",
                                label = "用户名"
                            )
                            QuietField(
                                value = password,
                                onValueChange = { password = it },
                                placeholder = "",
                                label = "密码",
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailing = {
                                    HeaderActionButton(
                                        icon = if (passwordVisible) {
                                            DianMaoIcons.EyeOff
                                        } else {
                                            DianMaoIcons.Eye
                                        },
                                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                        onClick = { passwordVisible = !passwordVisible },
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                        buttonSize = 34.dp,
                                        iconSize = 18.dp,
                                        tileShape = ChipShape
                                    )
                                }
                            )
                            TextButton(
                                onClick = { onSaveWebDav(webDavSettings) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                InlineButtonIcon(
                                    icon = DianMaoIcons.Save,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("保存设置")
                            }
                        }

                        FormSettingBlock(title = "云端同步", subtitle = "上传当前数据到 WebDAV，或从云端恢复") {
                            Button(
                                onClick = { onUploadWebDav(webDavSettings) },
                                enabled = webDavReady && !syncInProgress,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RowShape,
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                if (syncInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                } else {
                                    InlineButtonIcon(
                                        icon = DianMaoIcons.CloudUp,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Text(if (syncInProgress) "正在同步…" else "上传备份")
                            }
                            OutlinedButton(
                                onClick = { onRestoreWebDav(webDavSettings) },
                                enabled = webDavReady && !syncInProgress,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RowShape,
                                contentPadding = PaddingValues(vertical = 11.dp)
                            ) {
                                InlineButtonIcon(
                                    icon = DianMaoIcons.CloudDown,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("下载恢复")
                            }
                        }

                        FormSettingBlock(title = "自动备份", subtitle = "启动应用时自动上传到 WebDAV，最多每天一次") {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (autoBackupSettings.enabled) "自动备份已开启" else "开启自动备份",
                                        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = autoBackupStatusText(autoBackupSettings, webDavReady),
                                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                QuietSwitch(
                                    checked = autoBackupSettings.enabled,
                                    onCheckedChange = onToggleAutoBackup
                                )
                            }
                        }
                }
            }
        }
}

@Composable
internal fun ArchivedPanel(
    visible: Boolean,
    tasks: List<TaskUiModel>,
    onDismiss: () -> Unit,
    onUnarchive: (TaskUiModel) -> Unit
) {
    if (visible) {
        FullScreenScaffold(
            title = if (tasks.isEmpty()) "归档事项" else "归档事项 · ${tasks.size}",
            subtitle = "查看已归档的旧事项，恢复后回到待办",
            leadingIcon = DianMaoIcons.Archive,
            leadingIconTint = MaterialTheme.colorScheme.primary,
            onBack = onDismiss
        ) {
            if (tasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        IconTile(
                            icon = DianMaoIcons.Inventory,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                            iconSize = 24.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "暂无归档事项",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.mutedText)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        ArchivedTaskItem(
                            task = task,
                            onUnarchive = { onUnarchive(task) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun ArchivedTaskItem(
    task: TaskUiModel,
    onUnarchive: () -> Unit
) {
    val metaParts = buildList {
        add(task.quadrant.shortTitle)
        task.dueLabel?.let { add(it) }
        task.lastCompletedAtMillis?.let {
            add("归档于 ${DateTimeFormatters.formatTimestamp(it)}")
        }
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        shape = RowShape,
        border = quietBorder(0.12f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ColorDot(task.quadrant.color)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                val metaLine = buildString {
                    append(metaParts.joinToString(" · "))
                    if (!task.completionNote.isNullOrBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(task.completionNote)
                    }
                }
                if (metaLine.isNotBlank()) {
                    Text(
                        text = metaLine,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HeaderActionButton(
                icon = DianMaoIcons.Unarchive,
                contentDescription = "取消归档",
                onClick = onUnarchive,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
            )
        }
    }
}
