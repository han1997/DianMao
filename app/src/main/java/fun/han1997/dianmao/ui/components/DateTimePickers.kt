package `fun`.han1997.dianmao

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * DianMao date picker: Material 3 DatePickerDialog themed by DianMaoTheme.
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
    val initialStateMillis = selectedDateMillis?.let { localMidnightToUtcMillis(it) }
    val state = rememberDatePickerState(initialSelectedDateMillis = initialStateMillis)
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val utcMillis = state.selectedDateMillis
                    if (utcMillis != null) {
                        onConfirm(utcMillisToLocalDate(utcMillis))
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    ) {
        DatePicker(state = state)
    }
}

/**
 * DianMao time picker: Material 3 TimePicker (24-hour) inside a freeform
 * Dialog + Card container, per official M3 guidance — there is no
 * TimePickerDialog composable.
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
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = PanelShape,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.padding(20.dp)
            ) {
                TimePicker(state = state)
                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    androidx.compose.foundation.layout.Spacer(
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    TextButton(
                        onClick = { onConfirm(state.hour * 60 + state.minute) }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

private fun localMidnightToUtcMillis(localMidnightMillis: Long): Long {
    val localDate = Instant.ofEpochMilli(localMidnightMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}

private fun utcMillisToLocalDate(utcMillis: Long): LocalDate {
    return Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()
}

private fun defaultMinutesOfDay(): Int {
    val now = java.time.LocalTime.now()
    return now.hour * 60 + now.minute
}
