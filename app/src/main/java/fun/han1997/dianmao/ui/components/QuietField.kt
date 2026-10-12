package `fun`.han1997.dianmao

import `fun`.han1997.dianmao.DianMaoIcons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Prototype-first quiet field.
 *
 * The confirmed prototype draws every text input as `.field`: a 12dp radius
 * surface box with a hairline inset outline, plus a 2dp primary underline while
 * focused. A single line measures ~44dp, noticeably shorter than Material3's
 * `OutlinedTextField` (56dp minimum), so a plain M3 field visibly stretches the
 * form's vertical rhythm. Rebuild the exact prototype box from BasicTextField.
 */
@Composable
internal fun QuietField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = quietBodyFieldStyle(),
    minLines: Int = 1,
    maxLines: Int = 1,
    singleLine: Boolean = maxLines == 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    // `.field textarea` fixes min-height:72px for the multi-line detail box.
    contentMinHeight: androidx.compose.ui.unit.Dp = 0.dp,
    trailing: @Composable (() -> Unit)? = null
) {
    var focused by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RowShape)
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // .field padding: 10px 12px 9px
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 9.dp),
            verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = leadingIconTint,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(18.dp)
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .then(if (contentMinHeight > 0.dp) Modifier.heightIn(min = contentMinHeight) else Modifier)
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.58f)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { focused = it.isFocused },
                    textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    singleLine = singleLine,
                    minLines = minLines,
                    maxLines = maxLines,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions
                )
            }
            if (trailing != null) {
                trailing()
            }
        }
        // .field inset hairline; .field.focus adds a 2dp primary underline.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (focused) 2.dp else 1.dp)
                .background(
                    if (focused) MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                )
        )
    }
}

/** `.field .fval` — 16sp / 24 line height. */
@Composable
internal fun quietBodyFieldStyle(): TextStyle =
    MaterialTheme.typography.bodyLarge.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    )

/** `.field .fval.title` — 18sp / 500 / 25 line height. */
@Composable
internal fun quietTitleFieldStyle(): TextStyle =
    MaterialTheme.typography.titleMedium.copy(
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 25.sp,
        letterSpacing = 0.sp
    )