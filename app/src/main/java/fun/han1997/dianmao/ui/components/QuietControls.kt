package `fun`.han1997.dianmao

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

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
