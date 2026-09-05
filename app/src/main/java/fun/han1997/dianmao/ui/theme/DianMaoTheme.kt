package `fun`.han1997.dianmao

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
    val colorScheme = androidx.compose.material3.lightColorScheme(
        primary = Color(0xFFA84534),
        onPrimary = Color(0xFFFFFBFA),
        primaryContainer = Color(0xFFF5DED9),
        onPrimaryContainer = Color(0xFF3A100A),
        secondary = Color(0xFF2B6C9F),
        onSecondary = Color(0xFFF8FCFF),
        secondaryContainer = Color(0xFFDCEAF4),
        onSecondaryContainer = Color(0xFF071F31),
        background = Color(0xFFF8F8F5),
        onBackground = Color(0xFF1E211F),
        surface = Color(0xFFFFFCF9),
        onSurface = Color(0xFF1E211F),
        surfaceVariant = Color(0xFFEDEEEA),
        onSurfaceVariant = Color(0xFF5A625E),
        outline = Color(0xFFD9DDD8)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = androidx.compose.material3.Typography(
            headlineLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 27.sp,
                lineHeight = 33.sp,
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
            bodyLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.1.sp
            ),
            bodyMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal,
                letterSpacing = 0.1.sp
            ),
            labelMedium = androidx.compose.ui.text.TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.05.sp
            ),
            labelLarge = androidx.compose.ui.text.TextStyle(
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.1.sp
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
internal val RowShape = RoundedCornerShape(12.dp)
internal val ChipShape = RoundedCornerShape(8.dp)
internal val IconTileShape = RoundedCornerShape(14.dp)
