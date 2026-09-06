package `fun`.han1997.dianmao

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Shared branded container for date/time selection dialogs.
 *
 * Both the date and the time pickers render inside this app-owned chrome so
 * they read as one control family: a warm paper scrim
 * ([warmScrimColor], DESIGN.md "scrim") over the whole window, a centered
 * `PanelShape` card on the raised surface with a quiet hairline border, and an
 * app-styled action row. The underlying control stays the Material 3
 * `DatePicker` / `TimePicker` (native control, branded surface).
 */
private val warmScrimColor = Color(0xFF211815).copy(alpha = 0.16f)

@Composable
private fun BrandedPickerDialog(
    onDismiss: () -> Unit,
    title: String,
    confirmLabel: String = "确定",
    onConfirm: () -> Unit,
    wideContent: Boolean = false,
    content: @Composable () -> Unit
) {
    // The Material 3 DatePicker is designed at 360dp of content width. Wide
    // content (the date picker) must not be squeezed by card margins or its
    // date grid renders cramped; give it near-full-width chrome instead.
    val cardHorizontalPadding = if (wideContent) 0.dp else 24.dp
    val columnHorizontalPadding = if (wideContent) 0.dp else 20.dp
    val columnVerticalPadding = if (wideContent) 12.dp else 16.dp
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(warmScrimColor)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(min = 0.dp, max = if (wideContent) 376.dp else 360.dp)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
                    .padding(horizontal = cardHorizontalPadding)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = {}
                    ),
                shape = PanelShape,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = columnHorizontalPadding,
                        vertical = columnVerticalPadding
                    ),
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    content()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("取消")
                        }
                        TextButton(onClick = onConfirm) {
                            Text(confirmLabel)
                        }
                    }
                }
            }
        }
    }
}

/**
 * DianMao date picker: Material 3 DatePicker inside the shared branded
 * container.
 *
 * [selectedDateMillis] uses the app's local-midnight convention (the same
 * semantics as the persisted dueAtMillis date part). M3's picker state works
 * in UTC-midnight millis, so both directions convert explicitly.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DianMaoDatePickerDialog(
    selectedDateMillis: Long?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    val initialStateMillis = (selectedDateMillis ?: todayMidnightMillis())
        .let { localMidnightToUtcMillis(it) }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialStateMillis)
    BrandedPickerDialog(
        title = "选择到期日",
        wideContent = true,
        onDismiss = onDismiss,
        onConfirm = {
            val utcMillis = state.selectedDateMillis
            if (utcMillis != null) {
                onConfirm(utcMillisToLocalDate(utcMillis))
            }
        }
    ) {
        DatePicker(state = state)
    }
}

/**
 * DianMao time picker: Material 3 TimePicker (24-hour) inside the shared
 * branded container.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DianMaoTimePickerDialog(
    initialMinutesOfDay: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val initial = initialMinutesOfDay ?: defaultMinutesOfDay()
    val state = rememberTimePickerState(
        initialHour = initial / 60,
        initialMinute = initial % 60,
        is24Hour = true
    )
    BrandedPickerDialog(
        title = "选择提醒时间",
        onDismiss = onDismiss,
        onConfirm = { onConfirm(state.hour * 60 + state.minute) }
    ) {
        TimePicker(state = state)
    }
}

private fun localMidnightToUtcMillis(localMidnightMillis: Long): Long {
    val localDate = Instant.ofEpochMilli(localMidnightMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun todayMidnightMillis(): Long {
    return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}

private fun utcMillisToLocalDate(utcMillis: Long): LocalDate {
    return Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun defaultMinutesOfDay(): Int {
    val now = java.time.LocalTime.now()
    return now.hour * 60 + now.minute
}
