package com.ravanbarvar.patientmanager.ui.patients

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.ui.components.NumberStepper
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.PersianDigits

@Composable
fun BirthDatePickerDialog(
    initialDate: JalaliDate?,
    onDismiss: () -> Unit,
    onConfirm: (JalaliDate) -> Unit
) {
    val today = remember { JalaliDate.today() }
    val start = initialDate ?: JalaliDate(today.year - 25, 1, 1)
    var year by remember { mutableStateOf(start.year) }
    var month by remember { mutableStateOf(start.month) }
    var day by remember { mutableStateOf(start.day) }

    val monthLength = JalaliDate(year, month, 1).monthLength()
    if (day > monthLength) day = monthLength

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.patient_birth_date)) },
        text = {
            Column {
                Text(
                    text = JalaliDate(year, month, day).formatted(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.padding(vertical = 4.dp)) {
                    LabeledStepper(
                        label = "روز",
                        value = PersianDigits.of(day, 2),
                        onIncrease = { day = if (day >= monthLength) 1 else day + 1 },
                        onDecrease = { day = if (day <= 1) monthLength else day - 1 }
                    )
                    LabeledStepper(
                        label = "ماه",
                        value = PersianDigits.of(month, 2),
                        onIncrease = { month = if (month >= 12) 1 else month + 1 },
                        onDecrease = { month = if (month <= 1) 12 else month - 1 }
                    )
                    LabeledStepper(
                        label = "سال",
                        value = PersianDigits.of(year),
                        onIncrease = { year = (year + 1).coerceAtMost(today.year) },
                        onDecrease = { year = (year - 1).coerceAtLeast(1300) }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(JalaliDate(year, month, day)) }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Composable
private fun LabeledStepper(label: String, value: String, onIncrease: () -> Unit, onDecrease: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(2.dp))
        NumberStepper(value = value, onIncrease = onIncrease, onDecrease = onDecrease, width = 34.dp)
    }
}
