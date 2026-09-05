package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.backup.WebDavSettings
import `fun`.han1997.dianmao.backup.resolveBackupFileUrl
import `fun`.han1997.dianmao.data.DateTimeFormatters
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.ColorDot
import `fun`.han1997.dianmao.HeaderActionButton
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.FormSettingBlock
import `fun`.han1997.dianmao.webDavHost
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.FileDownload
import androidx.compose.material.icons.rounded.FileUpload
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
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
    status: String?,
    onDismiss: () -> Unit,
    onLocalExport: () -> Unit,
    onLocalImport: () -> Unit,
    onSaveWebDav: (WebDavSettings) -> Unit,
    onUploadWebDav: (WebDavSettings) -> Unit,
    onRestoreWebDav: (WebDavSettings) -> Unit
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

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(280)) { it / 4 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF211815).copy(alpha = 0.16f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .imePadding(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.88f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = SheetShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(38.dp)
                            .height(4.dp)
                            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.34f), CircleShape)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        IconTile(
                            icon = Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                            iconSize = 20.dp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "备份与恢复",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "本地文件与 WebDAV 同步",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HeaderActionButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "关闭备份面板",
                            onClick = onDismiss
                        )
                    }

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
                                    .padding(horizontal = 12.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                IconTile(
                                    icon = Icons.Rounded.CloudUpload,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f),
                                    iconSize = 18.dp
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
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
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.82f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Surface(
                                    color = if (webDavReady) {
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.10f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f)
                                    },
                                    contentColor = if (webDavReady) {
                                        MaterialTheme.colorScheme.secondary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
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
                                        contentPadding = PaddingValues(vertical = 13.dp)
                                    ) {
                                        InlineButtonIcon(
                                            icon = Icons.Rounded.FileUpload,
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
                                            icon = Icons.Rounded.FileUpload,
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
                                    contentPadding = PaddingValues(vertical = 13.dp)
                                ) {
                                    InlineButtonIcon(
                                        icon = Icons.Rounded.FileDownload,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("本地恢复")
                                }
                            }
                        }

                         FormSettingBlock(title = "WebDAV 配置", subtitle = "填目录地址自动建目录备份；以 .json 结尾则视为文件") {
                             OutlinedTextField(
                                 value = url,
                                 onValueChange = { url = it },
                                 modifier = Modifier.fillMaxWidth(),
                                 label = { Text("备份目录 URL") },
                                 placeholder = { Text("https://dav.example.com/dianmao/ 或 http://192.168.x.x/dav/") },
                                 supportingText = { Text("备份写入目录内 dianmao-backup-latest.json；目录不存在会自动创建") },
                                 singleLine = true,
                                 shape = RowShape,
                                 colors = quietTextFieldColors()
                             )
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("用户名") },
                                singleLine = true,
                                shape = RowShape,
                                colors = quietTextFieldColors()
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                modifier = Modifier.fillMaxWidth(),
                                label = { Text("密码") },
                                singleLine = true,
                                visualTransformation = if (passwordVisible) {
                                    VisualTransformation.None
                                } else {
                                    PasswordVisualTransformation()
                                },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) {
                                                Icons.Rounded.VisibilityOff
                                            } else {
                                                Icons.Rounded.Visibility
                                            },
                                            contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                },
                                shape = RowShape,
                                colors = quietTextFieldColors()
                            )
                            TextButton(
                                onClick = { onSaveWebDav(webDavSettings) },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.Save,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("保存设置")
                            }
                        }

                        FormSettingBlock(title = "云端同步", subtitle = "上传当前数据到 WebDAV，或从云端恢复") {
                            Button(
                                onClick = { onUploadWebDav(webDavSettings) },
                                enabled = webDavReady,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RowShape,
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.CloudUpload,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    containerColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("上传备份")
                            }
                            OutlinedButton(
                                onClick = { onRestoreWebDav(webDavSettings) },
                                enabled = webDavReady,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RowShape,
                                contentPadding = PaddingValues(vertical = 13.dp)
                            ) {
                                InlineButtonIcon(
                                    icon = Icons.Rounded.CloudDownload,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(6.dp))
                                Text("下载恢复")
                            }
                        }

                        // Status line pinned last
                        if (!status.isNullOrBlank()) {
                            val isError = status.contains("失败")
                            val isUpload = status.contains("上传")
                            val isDownload = status.contains("下载") || status.contains("从 WebDAV 恢复") || status.contains("从本地备份恢复")
                            val isExport = status.contains("导出")
                            val isSave = status.contains("设置已保存")
                            val statusIcon = when {
                                isError -> Icons.Rounded.Close
                                isUpload -> Icons.Rounded.CloudUpload
                                isDownload -> Icons.Rounded.CloudDownload
                                isExport -> Icons.Rounded.FileUpload
                                isSave -> Icons.Rounded.Save
                                else -> Icons.Rounded.CloudUpload
                            }
                            val statusTint = if (isError) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.onSecondaryContainer
                            }
                            Surface(
                                color = if (isError) {
                                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.62f)
                                } else {
                                    MaterialTheme.colorScheme.secondaryContainer
                                },
                                contentColor = statusTint,
                                shape = RowShape,
                                border = quietBorder(0.08f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 9.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    IconTile(
                                        icon = statusIcon,
                                        contentDescription = null,
                                        tint = statusTint,
                                        containerColor = statusTint.copy(alpha = 0.10f),
                                        iconSize = 14.dp
                                    )
                                    Text(
                                        text = status,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
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
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(200)) + slideInVertically(tween(280)) { it / 4 },
        exit = fadeOut(tween(160)) + slideOutVertically(tween(220)) { it / 4 }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF211815).copy(alpha = 0.16f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                )
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .fillMaxHeight(0.75f)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = SheetShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.safeDrawing)
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        IconTile(
                            icon = Icons.Rounded.Archive,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.62f),
                            iconSize = 20.dp
                        )
                        Text(
                            text = if (tasks.isEmpty()) "归档事项" else "归档事项 · ${tasks.size}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        HeaderActionButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "关闭",
                            onClick = onDismiss
                        )
                    }
                    Spacer(Modifier.height(12.dp))

                    if (tasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                IconTile(
                                    icon = Icons.Rounded.Inventory2,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
                                    iconSize = 24.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "暂无归档事项",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.weight(1f),
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            HeaderActionButton(
                icon = Icons.Rounded.Unarchive,
                contentDescription = "取消归档",
                onClick = onUnarchive,
                tint = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.50f)
            )
        }
    }
}
