package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskSubTask
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.IconTile
import `fun`.han1997.dianmao.InlineButtonIcon
import `fun`.han1997.dianmao.quietBorder
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

@Composable
internal fun TaskFormSection(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    emphasized: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = if (emphasized) RoundedCornerShape(20.dp) else PanelShape,
        color = if (emphasized) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.32f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.26f)
        },
        border = if (emphasized) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.18f))
        } else {
            quietBorder(0.14f)
        }
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = if (emphasized) 16.dp else 14.dp,
                vertical = if (emphasized) 14.dp else 11.dp
            ),
            verticalArrangement = Arrangement.spacedBy(if (emphasized) 12.dp else 9.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    IconTile(
                        icon = icon,
                        contentDescription = null,
                        tint = iconTint,
                        containerColor = iconTint.copy(alpha = 0.10f),
                        iconSize = 16.dp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
internal fun FormSettingBlock(
    title: String,
    subtitle: String? = null,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (enabled) 0.26f else 0.16f),
        border = quietBorder(if (enabled) 0.10f else 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (icon != null) {
                    IconTile(
                        icon = icon,
                        contentDescription = null,
                        tint = if (enabled) iconTint else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f),
                        containerColor = if (enabled) iconTint.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.20f),
                        iconSize = 15.dp
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 0.82f else 0.58f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            content()
        }
    }
}

@Composable
internal fun SubTaskEditor(
    subTasks: List<TaskSubTask>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAdd: () -> Unit,
    onTitleChange: (Long, String) -> Unit,
    onCheckedChange: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit
) {
    val completed = subTasks.count { it.completed }
    TaskFormSection(
        title = "子事项",
        subtitle = if (subTasks.isEmpty()) "可选" else "$completed/${subTasks.size} 已完成",
        icon = Icons.Rounded.CheckCircle,
        iconTint = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = draft,
                onValueChange = onDraftChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 56.dp),
                placeholder = { Text("添加一个小任务") },
                singleLine = true,
                shape = RowShape,
                textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { onAdd() }),
                colors = quietTextFieldColors()
            )
            FilledTonalButton(
                onClick = onAdd,
                enabled = draft.isNotBlank(),
                modifier = Modifier.heightIn(min = 56.dp),
                shape = RowShape,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                InlineButtonIcon(
                    icon = Icons.Rounded.Add,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    containerColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.10f)
                )
                Spacer(Modifier.width(4.dp))
                Text("添加")
            }
        }

        if (subTasks.isEmpty()) {
            Text(
                text = "还没有子事项，可在上方添加",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.62f)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                subTasks.forEach { item ->
                    SubTaskEditRow(
                        item = item,
                        onTitleChange = { onTitleChange(item.id, it) },
                        onCheckedChange = { onCheckedChange(item.id, it) },
                        onDelete = { onDelete(item.id) }
                    )
                }
            }
        }
    }
}

@Composable
internal fun SubTaskEditRow(
    item: TaskSubTask,
    onTitleChange: (String) -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RowShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        border = quietBorder(0.10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 2.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Checkbox(
                checked = item.completed,
                onCheckedChange = onCheckedChange
            )
            OutlinedTextField(
                value = item.title,
                onValueChange = onTitleChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 52.dp),
                placeholder = { Text("子事项") },
                singleLine = true,
                shape = RowShape,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = quietTextFieldColors()
            )
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Rounded.Delete,
                    contentDescription = "删除子事项",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.82f)
                )
            }
        }
    }
}

@Composable
internal fun SubTaskReadOnlySection(
    task: TaskUiModel,
    onToggleSubTask: (Long, Boolean) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "子事项",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = "已完成 ${task.completedSubTaskCount}/${task.subTasks.size}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RowShape,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
            border = quietBorder(0.10f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                task.subTasks.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = item.completed,
                                role = Role.Checkbox,
                                onValueChange = { checked -> onToggleSubTask(item.id, checked) }
                            )
                            .heightIn(min = 48.dp)
                            .padding(end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Checkbox(
                            checked = item.completed,
                            onCheckedChange = null
                        )
                        Text(
                            text = item.title,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.completed) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
