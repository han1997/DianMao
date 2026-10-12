package `fun`.han1997.dianmao

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Quiet control styling (ui-polish round 2, 2026-09).
 *
 * The default Material3 Switch and DropdownMenu look foreign against the
 * quiet-warm design language: a saturated unchecked track and a default
 * shadowed menu break the hairline/pale-surface vocabulary. These wrappers
 * are the only allowed entry points for newly-added switches/menus.
 */

@Composable
internal fun CountLabel(
    count: Int,
    suffix: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier
) {
    // Counts flicker on every task completion; a 150ms crossfade keeps the
    // update readable without feeling animated (state-change only motion).
    // transitionSpec lambdas are not @Composable scope, so read the flag
    // outside and pick snap()/tween() branches directly.
    val animationsEnabled = LocalAnimationsEnabled.current
    AnimatedContent(
        targetState = count,
        transitionSpec = {
            val spec = if (animationsEnabled) androidx.compose.animation.core.tween<Float>(150)
                else androidx.compose.animation.core.snap()
            fadeIn(spec) togetherWith fadeOut(spec)
        },
        label = "count",
        modifier = modifier
    ) { value ->
        Text(
            text = if (suffix.isEmpty()) "$value" else "$value $suffix",
            style = style,
            color = color
        )
    }
}

@Composable
internal fun QuietSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = SwitchDefaults.colors(
            checkedTrackColor = MaterialTheme.colorScheme.secondary,
            checkedThumbColor = MaterialTheme.colorScheme.surface,
            uncheckedTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = DianMaoAlpha.subtle),
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
            uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.24f)
        )
    )
}

/**
 * Quiet segmented control (prototype-first, 2026-10).
 *
 * Mirrors the prototype `.segmented` / `.seg` exactly: a connected row where
 * each segment is 9dp/4dp padding at labelMedium, joined by a 60%-alpha outline
 * with a 1dp overlap, and the selected segment filled with primaryContainer.
 * M3 `SegmentedButton` is NOT used here because it adds a leading check icon and
 * its own padding/height, which visibly diverges from the confirmed prototype.
 */
@Composable
internal fun QuietSegmented(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
    /** Optional leading content per segment (e.g. an 8dp quadrant color dot). */
    leading: (@Composable (Int) -> Unit)? = null
) {
    Row(modifier = modifier.fillMaxWidth()) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            val shape = when (index) {
                0 -> RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp)
                options.lastIndex -> RoundedCornerShape(topEnd = 12.dp, bottomEnd = 12.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (index > 0) Modifier.offset(x = (-1).dp) else Modifier)
                    .clip(shape)
                    .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.60f), shape)
                    .clickable(role = Role.RadioButton) { onSelect(index) }
                    .padding(horizontal = 4.dp, vertical = 9.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    leading?.invoke(index)
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.sp),
                        color = if (selected) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        },
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuietDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RowShape)) {
        // MenuDefaults.menuColors in M3 1.3 accepts containerColor; keep the
        // menu on the quiet surface + hairline border, no default shadow.
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismissRequest,
            modifier = modifier,
            shape = RowShape,
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
            border = quietBorder(0.14f),
            content = content
        )
    }
}
