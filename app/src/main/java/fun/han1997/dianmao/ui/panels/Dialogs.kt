package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
internal fun DeleteTaskDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    QuietDialogContainer(
        onDismiss = onDismiss,
        title = "删除事项",
        leadingIcon = Icons.Rounded.Delete,
        leadingIconTint = MaterialTheme.colorScheme.error,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
            }
        }
    ) {
        Text(
            text = "确定删除“${task.title}”？删除后不会保留提醒和完成备注。",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
internal fun UncompleteRecurringTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    QuietDialogContainer(
        onDismiss = onDismiss,
        title = "取消完成重复事项？",
        leadingIcon = Icons.Rounded.Replay,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            TextButton(onClick = onConfirm) {
                Text("确认取消完成")
            }
        }
    ) {
        Text(
            text = "这是一个重复事项，取消完成后将重新进入待办状态。是否继续？",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
internal fun CompletionNoteDialog(
    task: TaskUiModel,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var note by rememberSaveable(task.id) { mutableStateOf(task.completionNote.orEmpty()) }

    QuietDialogContainer(
        onDismiss = onDismiss,
        title = if (task.completed) "编辑完成备注" else "完成并写备注",
        leadingIcon = Icons.Rounded.EditNote,
        leadingIconTint = MaterialTheme.colorScheme.primary,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("稍后再写")
            }
            TextButton(onClick = { onSave(note) }) {
                Text("保存备注")
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("心得或复盘") },
                placeholder = { Text("这件事完成后，我学到或确认了什么？") },
                minLines = 3,
                maxLines = 6,
                shape = RowShape,
                textStyle = MaterialTheme.typography.bodyMedium,
                colors = quietTextFieldColors()
            )
        }
    }
}
