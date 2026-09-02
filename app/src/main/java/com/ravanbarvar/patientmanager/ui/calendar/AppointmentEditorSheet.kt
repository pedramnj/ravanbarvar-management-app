package com.ravanbarvar.patientmanager.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentStatus
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.ui.components.NumberStepper
import com.ravanbarvar.patientmanager.ui.theme.SagePrimary
import com.ravanbarvar.patientmanager.ui.theme.WarningAmber as WarningAmberColor
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.PersianDigits
import com.ravanbarvar.patientmanager.util.minutesToDurationLabel
import com.ravanbarvar.patientmanager.util.minutesToPersianClock

private val durationOptions = listOf(15, 30, 45, 60, 90)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppointmentEditorSheet(
    sheetState: SheetState,
    initialDate: JalaliDate,
    patients: List<PatientEntity>,
    editing: AppointmentWithPatientName?,
    appointmentRepository: AppointmentRepository,
    onDismiss: () -> Unit,
    onSave: (patientId: Long, date: JalaliDate, startMinutes: Int, durationMinutes: Int, notes: String, feeAmount: Long?, isPaid: Boolean) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var date by remember { mutableStateOf(editing?.let { JalaliDate.fromEpochDay(it.dateEpochDay) } ?: initialDate) }
    var selectedPatientId by remember { mutableStateOf(editing?.patientId ?: patients.firstOrNull()?.id) }
    var hour by remember { mutableStateOf((editing?.startMinutes ?: (9 * 60)) / 60) }
    var minute by remember { mutableStateOf((editing?.startMinutes ?: (9 * 60)) % 60) }
    var duration by remember { mutableStateOf(editing?.durationMinutes ?: 45) }
    var notes by remember { mutableStateOf(editing?.notes ?: "") }
    var feeText by remember { mutableStateOf(editing?.feeAmount?.toString() ?: "") }
    var isPaid by remember { mutableStateOf(editing?.isPaid ?: false) }
    var patientMenuExpanded by remember { mutableStateOf(false) }

    val appointmentsOnDate by remember(date) {
        appointmentRepository.observeForDate(date.toEpochDay())
    }.collectAsState(initial = emptyList())

    val proposedStart = hour * 60 + minute
    val proposedEnd = proposedStart + duration
    val conflict = appointmentsOnDate.firstOrNull { other ->
        other.id != (editing?.id ?: -1L) &&
            other.status != AppointmentStatus.CANCELED &&
            proposedStart < other.startMinutes + other.durationMinutes &&
            other.startMinutes < proposedEnd
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            Text(
                text = stringResource(if (editing == null) R.string.add_appointment else R.string.edit_appointment),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(18.dp))

            // Date stepper
            Text(stringResource(R.string.select_date), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { date = date.plusDays(-1) }) {
                    Icon(Icons.Filled.ChevronRight, contentDescription = null)
                }
                Text(
                    text = date.formatted(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(onClick = { date = date.plusDays(1) }) {
                    Icon(Icons.Filled.ChevronLeft, contentDescription = null)
                }
            }

            Spacer(Modifier.height(14.dp))

            // Patient picker
            Text(stringResource(R.string.select_patient), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            if (patients.isEmpty()) {
                Text(
                    stringResource(R.string.no_patients_for_appointment),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                val selectedPatient = patients.firstOrNull { it.id == selectedPatientId }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPatient?.let { "${it.firstName} ${it.lastName}" } ?: stringResource(R.string.choose_patient_placeholder),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { patientMenuExpanded = true }
                    )
                    DropdownMenu(
                        expanded = patientMenuExpanded,
                        onDismissRequest = { patientMenuExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        patients.forEach { p ->
                            DropdownMenuItem(
                                text = { Text("${p.firstName} ${p.lastName}") },
                                onClick = {
                                    selectedPatientId = p.id
                                    patientMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // Time steppers
            Text(stringResource(R.string.appointment_time), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumberStepper(
                    value = PersianDigits.of(hour, 2),
                    onIncrease = { hour = (hour + 1).coerceIn(0, 23) },
                    onDecrease = { hour = (hour - 1).coerceIn(0, 23) }
                )
                Text(":", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 6.dp))
                NumberStepper(
                    value = PersianDigits.of(minute, 2),
                    onIncrease = { minute = ((minute + 5) % 60) },
                    onDecrease = { minute = ((minute - 5 + 60) % 60) }
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = minutesToPersianClock(hour * 60 + minute),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(14.dp))

            Text(stringResource(R.string.appointment_duration), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState())
            ) {
                durationOptions.forEach { option ->
                    FilterChip(
                        selected = duration == option,
                        onClick = { duration = option },
                        label = { Text(minutesToDurationLabel(option), maxLines = 1) }
                    )
                }
            }

            if (conflict != null) {
                Spacer(Modifier.height(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(WarningAmberColor.copy(alpha = 0.14f))
                        .padding(12.dp)
                ) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = WarningAmberColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.appointment_conflict_warning, conflict.patientFullName),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(stringResource(R.string.appointment_notes)) },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value = feeText,
                onValueChange = { feeText = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.appointment_fee)) },
                placeholder = { Text(stringResource(R.string.appointment_fee_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { isPaid = !isPaid }
            ) {
                Checkbox(
                    checked = isPaid,
                    onCheckedChange = { isPaid = it },
                    colors = CheckboxDefaults.colors(checkedColor = SagePrimary)
                )
                Text(stringResource(R.string.appointment_paid), style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (onDelete != null) {
                    OutlinedButton(
                        onClick = onDelete,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.height(50.dp)
                    ) {
                        Text(stringResource(R.string.delete))
                    }
                }
                Button(
                    onClick = {
                        val pid = selectedPatientId
                        if (pid != null) {
                            onSave(pid, date, hour * 60 + minute, duration, notes, feeText.toLongOrNull(), isPaid)
                        }
                    },
                    enabled = selectedPatientId != null,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(Modifier.height(6.dp))
            TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}
