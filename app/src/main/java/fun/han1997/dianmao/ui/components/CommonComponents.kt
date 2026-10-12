package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons

import `fun`.han1997.dianmao.data.TaskPriority
import `fun`.han1997.dianmao.quietBorder
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun AppCompassMark(
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val size = if (compact) 32.dp else 42.dp
    Surface(
        modifier = modifier.size(size),
        shape = IconTileShape,
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.primary,
        border = quietBorder(0.12f)
    ) {
        Box(
            modifier = Modifier.padding(if (compact) 7.dp else 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CompassCell(TaskPriority.ImportantUrgent.color, Modifier.weight(1f))
                    CompassCell(TaskPriority.ImportantNotUrgent.color, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    CompassCell(TaskPriority.NotImportantUrgent.color, Modifier.weight(1f))
                    CompassCell(TaskPriority.NotImportantNotUrgent.color, Modifier.weight(1f))
                }
            }
            Box(
                modifier = Modifier
                    .size(if (compact) 8.dp else 10.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.44f), CircleShape)
            )
        }
    }
}

@Composable
internal fun CompassCell(color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(color.copy(alpha = DianMaoAlpha.strong), RoundedCornerShape(4.dp))
    )
}

@Composable
internal fun IconTile(
    icon: ImageVector,
    contentDescription: String?,
    tint: Color,
    modifier: Modifier = Modifier,
    containerColor: Color = tint.copy(alpha = 0.10f),
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    shape: Shape? = null
) {
    // Explicit tile sizes keep the three scales deliberate: compact 20dp
    // (inline meta chips), standard 30dp (sections and rows), header 34dp.
    val tileSize = when {
        iconSize <= 14.dp -> 20.dp
        else -> 30.dp
    }
    Surface(
        modifier = modifier.size(tileSize),
        shape = shape ?: if (iconSize <= 14.dp) ChipShape else IconTileShape,
        color = containerColor,
        contentColor = tint
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
internal fun HeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    buttonSize: androidx.compose.ui.unit.Dp = 40.dp,
    iconSize: androidx.compose.ui.unit.Dp = 18.dp,
    tileShape: Shape = IconTileShape
) {
    // Prototype-first: the confirmed prototype lays out every header action at
    // 40x40 (with a 34x34 tonal tile inside). Material3's IconButton enforces a
    // 48dp minimum, which widens the row and clips the title, so build the button
    // from Box + clickable: 40dp layout box, and the 40dp target is still a
    // comfortable size (the prototype's own target).
    Box(
        modifier = modifier
            .size(buttonSize)
            .clip(IconTileShape)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        IconTile(
            icon = icon,
            contentDescription = contentDescription,
            tint = tint,
            containerColor = containerColor,
            iconSize = iconSize,
            modifier = Modifier.size(34.dp),
            shape = tileShape
        )
    }
}

@Composable
internal fun InlineButtonIcon(
    icon: ImageVector,
    tint: Color,
    containerColor: Color = tint.copy(alpha = 0.12f)
) {
    // Prototype .btn .itile: a 20dp tile with an 8dp radius holding a 15dp
    // icon (btnIcon uses s15). IconTile's default 30dp tile is too large here.
    Surface(
        modifier = Modifier.size(20.dp),
        shape = ChipShape,
        color = containerColor,
        contentColor = tint
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

@Composable
internal fun ColorDot(color: Color, modifier: Modifier = Modifier, size: androidx.compose.ui.unit.Dp = 12.dp) {
    Box(
        modifier = modifier
            .size(size)
            .background(color, CircleShape)
            .border(1.2.dp, MaterialTheme.colorScheme.surface.copy(alpha = if (color.luminance() < 0.5f) 0.38f else 0.72f), CircleShape)
    )
}

@Composable
internal fun CompletionMark(completed: Boolean, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(18.dp)
            .background(
                if (completed) color else Color.Transparent,
                CircleShape
            )
            .border(
                1.5.dp,
                if (completed) color else MaterialTheme.colorScheme.outline,
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (completed) {
            Icon(
                imageVector = DianMaoIcons.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(12.dp)
            )
        }
    }
}

@Composable
internal fun CompactMeta(
    icon: ImageVector,
    text: String,
    tint: Color = MaterialTheme.colorScheme.secondary
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f), ChipShape)
            .padding(start = 3.dp, top = 3.dp, end = 7.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        IconTile(
            icon = icon,
            contentDescription = null,
            tint = tint.copy(alpha = DianMaoAlpha.strong),
            containerColor = tint.copy(alpha = 0.10f),
            iconSize = 12.dp
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                letterSpacing = 0.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun InlineStatDot(count: Int, label: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$count $label",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            // Prototype .statdot colours the whole label with --c (no alpha).
            color = color
        )
    }
}

@Composable
internal fun SummaryPill(
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    val pillModifier = if (onClick != null) {
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f), ChipShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 7.dp, vertical = 4.dp)
    } else {
        modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f), ChipShape)
            .padding(horizontal = 7.dp, vertical = 4.dp)
    }
    Row(
        modifier = pillModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong)
        )
    }
}
