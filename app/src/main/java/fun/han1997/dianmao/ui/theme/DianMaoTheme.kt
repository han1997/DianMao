package `fun`.han1997.dianmao

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun DianMaoTheme(content: @Composable () -> Unit) {
    val colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 27.sp,
                lineHeight = 33.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            headlineSmall = androidx.compose.ui.text.TextStyle(
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            titleLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 20.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            titleMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            titleSmall = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.sp
            ),
            bodyLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
            ),
            bodySmall = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.sp
            ),
            labelLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            labelMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            ),
            labelSmall = androidx.compose.ui.text.TextStyle(
                fontSize = 11.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.sp
            )
        ),
        content = content
    )
}

@Composable
internal fun quietBorder(alpha: Float = 0.22f): BorderStroke {
    return BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = alpha))
}

@Composable
internal fun quietTextFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f),
    focusedIndicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
    disabledIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.10f)
)

internal val PanelShape = RoundedCornerShape(18.dp)
internal val SheetShape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
internal val InlineShape = RoundedCornerShape(14.dp)
internal val RowShape = RoundedCornerShape(12.dp)
internal val ChipShape = RoundedCornerShape(8.dp)
internal val IconTileShape = RoundedCornerShape(14.dp)
internal val PillShape = RoundedCornerShape(999.dp)

/** DESIGN.md "scrim": the app's warm dim color for overlays and dialogs. */
internal val warmScrimColor = Color(0xFF211815).copy(alpha = 0.16f)

/**
 * Light scheme — the DianMao warm paper identity from DESIGN.md.
 */
private fun lightColorScheme() = androidx.compose.material3.lightColorScheme(
    primary = Color(0xFFA84534),
    onPrimary = Color(0xFFFFFBFA),
    primaryContainer = Color(0xFFF5DED9),
    onPrimaryContainer = Color(0xFF3A100A),
    secondary = Color(0xFF2B6C9F),
    onSecondary = Color(0xFFF8FCFF),
    secondaryContainer = Color(0xFFDCEAF4),
    onSecondaryContainer = Color(0xFF071F31),
    error = Color(0xFF9C3B2E),
    onError = Color(0xFFFFFBFA),
    errorContainer = Color(0xFFF3DAD5),
    onErrorContainer = Color(0xFF3A100A),
    background = Color(0xFFF8F8F5),
    onBackground = Color(0xFF1E211F),
    surface = Color(0xFFFFFCF9),
    onSurface = Color(0xFF1E211F),
    surfaceVariant = Color(0xFFEDEEEA),
    onSurfaceVariant = Color(0xFF5A625E),
    outline = Color(0xFFD9DDD8)
)

/**
 * Dark scheme — the warm paper identity carried into a deep warm brown-grey
 * world. Terracotta and calendar blue are lightened for contrast on dark
 * surfaces; containers invert to deep tinted washes; quadrant tints stay
 * low-alpha so the quiet language holds at night.
 */
private fun darkColorScheme() = androidx.compose.material3.darkColorScheme(
    primary = Color(0xFFE0806C),
    onPrimary = Color(0xFF3A100A),
    primaryContainer = Color(0xFF5C2418),
    onPrimaryContainer = Color(0xFFF5DED9),
    secondary = Color(0xFF7FB0D8),
    onSecondary = Color(0xFF071F31),
    secondaryContainer = Color(0xFF1D3A50),
    onSecondaryContainer = Color(0xFFDCEAF4),
    error = Color(0xFFE8A08F),
    onError = Color(0xFF441008),
    errorContainer = Color(0xFF5C2117),
    onErrorContainer = Color(0xFFF3DAD5),
    background = Color(0xFF171412),
    onBackground = Color(0xFFEAE5E0),
    surface = Color(0xFF201C19),
    onSurface = Color(0xFFEAE5E0),
    surfaceVariant = Color(0xFF2C2825),
    onSurfaceVariant = Color(0xFFA8A29B),
    outline = Color(0xFF4A453F)
)
