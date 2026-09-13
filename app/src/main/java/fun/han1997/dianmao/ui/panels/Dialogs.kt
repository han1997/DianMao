package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.data.RepeatRule
import `fun`.han1997.dianmao.data.TaskUiModel
import `fun`.han1997.dianmao.quietTextFieldColors
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/** One repeat preset row in the repeat picker. */
internal enum class RepeatPreset(val label: String) {
    None("不重复"),
    Daily("每天"),
    Workdays("工作日"),
    Weekly("每周"),
    Monthly("每月"),
    Yearly("每年"),
    Count("计数"),
    Custom("自定义")
}

private val WORKDAY_SET = setOf(1, 2, 3, 4, 5)
private const val MAX_REPEAT_INTERVAL = 99

/**
 * Reverse-maps a persisted repeat triple onto the picker preset. Anything
 * beyond the plain presets (custom interval or weekday mix) maps to Custom.
 */
internal fun repeatPresetFor(rule: RepeatRule, interval: Int, weekdays: Set<Int>): RepeatPreset {
    val normalizedInterval = interval.coerceAtLeast(1)
    return when (rule) {
        RepeatRule.None -> RepeatPreset.None
        RepeatRule.Count -> RepeatPreset.Count
        RepeatRule.Daily -> if (normalizedInterval == 1) RepeatPreset.Daily else RepeatPreset.Custom
        RepeatRule.Monthly -> if (normalizedInterval == 1) RepeatPreset.Monthly else RepeatPreset.Custom
        RepeatRule.Yearly -> if (normalizedInterval == 1) RepeatPreset.Yearly else RepeatPreset.Custom
        RepeatRule.Weekly -> when {
            normalizedInterval == 1 && weekdays.isEmpty() -> RepeatPreset.Weekly
            normalizedInterval == 1 && weekdays.filter { it in 1..7 }.toSet() == WORKDAY_SET -> RepeatPreset.Workdays
            else -> RepeatPreset.Custom
        }
    }
}

/**
 * Branded repeat-rule picker replacing the old dropdown: preset rows plus an
 * inline custom editor (frequency, interval stepper, weekday chips) that only
 * appears when 自定义 is selected. Confirms as one (rule, interval, weekdays)
 * triple per the single-shot dialog contract.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun RepeatRulePickerDialog(
    selectedRule: RepeatRule,
    selectedInterval: Int,
    selectedWeekdays: Set<Int>,
    onDismiss: () -> Unit,
    onConfirm: (RepeatRule, Int, Set<Int>) -> Unit
) {
    var preset by rememberSaveable { mutableStateOf(repeatPresetFor(selectedRule, selectedInterval, selectedWeekdays)) }
    var customRule by rememberSaveable {
        mutableStateOf(if (selectedRule in listOf(RepeatRule.Daily, RepeatRule.Weekly, RepeatRule.Monthly, RepeatRule.Yearly)) {
            selectedRule
        } else {
            RepeatRule.Weekly
        })
    }
    var customInterval by rememberSaveable { mutableStateOf(selectedInterval.coerceAtLeast(1)) }
    var customWeekdays by rememberSaveable {
        mutableStateOf(selectedWeekdays.filter { it in 1..7 }.toSet().ifEmpty { setOf(1) })
    }

    fun confirm() {
        val (rule, interval, weekdays) = when (preset) {
            RepeatPreset.None -> Triple(RepeatRule.None, 1, emptySet())
            RepeatPreset.Daily -> Triple(RepeatRule.Daily, 1, emptySet())
            RepeatPreset.Workdays -> Triple(RepeatRule.Weekly, 1, WORKDAY_SET)
            RepeatPreset.Weekly -> Triple(RepeatRule.Weekly, 1, emptySet())
            RepeatPreset.Monthly -> Triple(RepeatRule.Monthly, 1, emptySet())
            RepeatPreset.Yearly -> Triple(RepeatRule.Yearly, 1, emptySet())
            RepeatPreset.Count -> Triple(RepeatRule.Count, 1, emptySet())
            RepeatPreset.Custom -> Triple(
                customRule,
                customInterval.coerceAtLeast(1),
                if (customRule == RepeatRule.Weekly) customWeekdays.filter { it in 1..7 }.toSet() else emptySet()
            )
        }
        onConfirm(rule, interval, weekdays)
    }

    QuietDialogContainer(
        onDismiss = onDismiss,
        title = "重复规则",
        leadingIcon = Icons.Rounded.Repeat,
        leadingIconTint = MaterialTheme.colorScheme.secondary,
        actions = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
            TextButton(onClick = { confirm() }) {
                Text("确定")
            }
        }
    ) {
        Column(
            modifier = Modifier
                .heightIn(max = 420.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            RepeatPreset.entries.forEach { item ->
                val selected = item == preset
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(role = Role.Button) { preset = item }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selected) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Spacer(Modifier.size(18.dp))
                    }
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 0.sp),
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                        color = if (selected) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            if (preset == RepeatPreset.Custom) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
                )
                Text(
                    text = "频率",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RepeatRule.Daily to "天",
                        RepeatRule.Weekly to "周",
                        RepeatRule.Monthly to "月",
                        RepeatRule.Yearly to "年"
                    ).forEach { (rule, label) ->
                        val selected = customRule == rule
                        Surface(
                            shape = ChipShape,
                            color = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                            },
                            contentColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            border = if (selected) null else quietBorder(0.10f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(role = Role.Button) { customRule = rule }
                        ) {
                            Text(
                                text = "每 $label",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                                maxLines = 1
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "间隔",
                        style = MaterialTheme.typography.bodyMedium.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.weight(1f))
                    IconButton(
                        onClick = { customInterval = (customInterval - 1).coerceAtLeast(1) },
                        enabled = customInterval > 1
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Remove,
                            contentDescription = "减少间隔",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${customInterval.coerceAtLeast(1)}",
                        style = MaterialTheme.typography.titleMedium.copy(letterSpacing = 0.sp),
                        fontWeight = FontWeight.SemiBold
                    )
                    IconButton(
                        onClick = { customInterval = (customInterval + 1).coerceAtMost(MAX_REPEAT_INTERVAL) },
                        enabled = customInterval < MAX_REPEAT_INTERVAL
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "增加间隔",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (customRule == RepeatRule.Weekly) {
                    Text(
                        text = "重复周几",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        (1..7).forEach { day ->
                            val selected = day in customWeekdays
                            Surface(
                                shape = ChipShape,
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f)
                                },
                                contentColor = if (selected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                border = if (selected) null else quietBorder(0.10f),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable(role = Role.Button) {
                                        customWeekdays = if (selected) {
                                            customWeekdays - day
                                        } else {
                                            customWeekdays + day
                                        }
                                    }
                            ) {
                                Text(
                                    text = weekdayChipLabel(day),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun weekdayChipLabel(isoDay: Int): String {
    return when (isoDay) {
        1 -> "一"
        2 -> "二"
        3 -> "三"
        4 -> "四"
        5 -> "五"
        6 -> "六"
        else -> "日"
    }
}
