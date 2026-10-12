package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Shared full-screen secondary destination shell.
 *
 * Secondary surfaces (backup/settings, archive, task add/edit, recurring
 * history) render as full-screen destinations like [TaskDetailScreen] — an
 * edge-to-edge [Surface] over the app background with a quiet anchored top bar
 * and a single [WindowInsets.safeDrawing] padded content column. Content width
 * is capped on large/expanded windows so forms read as a centered column
 * instead of stretching edge to edge.
 */
@Composable
internal fun FullScreenScaffold(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    actions: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            FullScreenTopBar(
                title = title,
                subtitle = subtitle,
                leadingIcon = leadingIcon,
                leadingIconTint = leadingIconTint,
                onBack = onBack,
                actions = actions
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .align(Alignment.Center)
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
internal fun FullScreenTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    actions: @Composable () -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = quietBorder(0.10f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            HeaderActionButton(
                icon = DianMaoIcons.ArrowBack,
                contentDescription = "返回",
                onClick = onBack
            )
            if (leadingIcon != null) {
                Box(modifier = Modifier.padding(start = 2.dp)) {
                    IconTile(
                        icon = leadingIcon,
                        contentDescription = null,
                        tint = leadingIconTint,
                        containerColor = leadingIconTint.copy(alpha = 0.10f),
                        iconSize = 18.dp
                    )
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DianMaoAlpha.strong),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            actions()
        }
    }
}
