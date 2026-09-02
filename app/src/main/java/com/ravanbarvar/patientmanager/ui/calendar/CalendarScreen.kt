package com.ravanbarvar.patientmanager.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotInterested
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentStatus
import com.ravanbarvar.patientmanager.ui.currentApp
import com.ravanbarvar.patientmanager.ui.theme.LavenderSecondary
import com.ravanbarvar.patientmanager.ui.theme.SagePrimary
import com.ravanbarvar.patientmanager.ui.theme.statusCanceled
import com.ravanbarvar.patientmanager.ui.theme.statusDone
import com.ravanbarvar.patientmanager.ui.theme.statusScheduled
import com.ravanbarvar.patientmanager.util.JalaliCalendar
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.PersianDigits
import com.ravanbarvar.patientmanager.util.minutesToPersianClock
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val app = currentApp()
    val viewModel: CalendarViewModel = viewModel(
        factory = viewModelFactory {
            initializer { CalendarViewModel(app.appointmentRepository, app.patientRepository) }
        }
    )

    val displayedMonth by viewModel.displayedMonth.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val appointments by viewModel.appointmentsForSelectedDate.collectAsState()
    val appointmentDates by viewModel.appointmentDatesInMonth.collectAsState()
    val patients by viewModel.patients.collectAsState()

    var showEditor by remember { mutableStateOf(false) }
    var editingAppointment by remember { mutableStateOf<AppointmentWithPatientName?>(null) }
    var appointmentPendingDelete by remember { mutableStateOf<AppointmentWithPatientName?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingAppointment = null
                    showEditor = true
                },
                containerColor = SagePrimary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Filled.CalendarToday, contentDescription = null) },
                text = { Text(stringResource(R.string.add_appointment)) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = stringResource(R.string.calendar_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)
            )

            MonthHeader(
                displayedMonth = displayedMonth,
                onPrev = viewModel::goToPrevMonth,
                onNext = viewModel::goToNextMonth,
                onToday = viewModel::goToToday
            )

            WeekdayHeaderRow()

            MonthGrid(
                displayedMonth = displayedMonth,
                selectedDate = selectedDate,
                appointmentDates = appointmentDates,
                onSelectDate = viewModel::selectDate
            )

            Spacer(Modifier.height(8.dp))

            DayAgenda(
                selectedDate = selectedDate,
                appointments = appointments,
                onAppointmentClick = {
                    editingAppointment = it
                    showEditor = true
                },
                onDeleteClick = { appointmentPendingDelete = it },
                onMarkDone = { viewModel.setStatus(it, AppointmentStatus.DONE) },
                onMarkCanceled = { viewModel.setStatus(it, AppointmentStatus.CANCELED) }
            )
        }
    }

    fun closeEditor() {
        scope.launch {
            sheetState.hide()
            showEditor = false
        }
    }

    if (showEditor) {
        AppointmentEditorSheet(
            sheetState = sheetState,
            initialDate = selectedDate,
            patients = patients,
            editing = editingAppointment,
            onDismiss = { closeEditor() },
            onSave = { patientId, date, startMinutes, durationMinutes, notes ->
                val editing = editingAppointment
                if (editing == null) {
                    viewModel.addAppointment(patientId, date, startMinutes, durationMinutes, notes)
                } else {
                    viewModel.updateAppointment(editing, patientId, date, startMinutes, durationMinutes, notes)
                }
                closeEditor()
            },
            onDelete = editingAppointment?.let { appt ->
                {
                    viewModel.deleteAppointment(appt)
                    closeEditor()
                }
            }
        )
    }

    appointmentPendingDelete?.let { appt ->
        AlertDialog(
            onDismissRequest = { appointmentPendingDelete = null },
            title = { Text(stringResource(R.string.confirm_delete_appointment_title)) },
            text = { Text(stringResource(R.string.confirm_delete_appointment_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteAppointment(appt)
                    appointmentPendingDelete = null
                }) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { appointmentPendingDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun MonthHeader(displayedMonth: JalaliDate, onPrev: () -> Unit, onNext: () -> Unit, onToday: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onNext) {
            Icon(Icons.Filled.ChevronLeft, contentDescription = stringResource(R.string.next_month))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onToday)
        ) {
            Text(
                text = "${displayedMonth.monthName()} ${PersianDigits.of(displayedMonth.year)}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        IconButton(onClick = onPrev) {
            Icon(Icons.Filled.ChevronRight, contentDescription = stringResource(R.string.prev_month))
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        JalaliCalendar.weekdayShortNames.forEach { name ->
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MonthGrid(
    displayedMonth: JalaliDate,
    selectedDate: JalaliDate,
    appointmentDates: Set<Long>,
    onSelectDate: (JalaliDate) -> Unit
) {
    val firstOfMonth = JalaliDate(displayedMonth.year, displayedMonth.month, 1)
    val leadingBlanks = firstOfMonth.dayOfWeekIndex()
    val monthLength = firstOfMonth.monthLength()
    val today = remember { JalaliDate.today() }

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        userScrollEnabled = false
    ) {
        items(leadingBlanks) {
            Box(modifier = Modifier.aspectRatio(1f))
        }
        items(monthLength) { index ->
            val day = index + 1
            val date = JalaliDate(displayedMonth.year, displayedMonth.month, day)
            val isSelected = date == selectedDate
            val isToday = date == today
            val hasAppointments = appointmentDates.contains(date.toEpochDay())

            Box(
                modifier = Modifier
                    .aspectRatio(1f)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .then(
                        if (isSelected) Modifier.background(SagePrimary)
                        else if (isToday) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                        else Modifier
                    )
                    .clickable { onSelectDate(date) },
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = PersianDigits.of(day),
                        style = MaterialTheme.typography.bodyMedium,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                            else -> MaterialTheme.colorScheme.onBackground
                        },
                        fontWeight = if (isSelected || isToday) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (hasAppointments) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else LavenderSecondary)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayAgenda(
    selectedDate: JalaliDate,
    appointments: List<AppointmentWithPatientName>,
    onAppointmentClick: (AppointmentWithPatientName) -> Unit,
    onDeleteClick: (AppointmentWithPatientName) -> Unit,
    onMarkDone: (AppointmentWithPatientName) -> Unit,
    onMarkCanceled: (AppointmentWithPatientName) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Text(
            text = "${stringResource(R.string.appointments_of_day)} · ${selectedDate.formatted()}",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))

        if (appointments.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Filled.EventBusy,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.no_appointments),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(appointments) { appt ->
                    AppointmentRow(
                        appointment = appt,
                        onClick = { onAppointmentClick(appt) },
                        onDelete = { onDeleteClick(appt) },
                        onMarkDone = { onMarkDone(appt) },
                        onMarkCanceled = { onMarkCanceled(appt) }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppointmentRow(
    appointment: AppointmentWithPatientName,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onMarkDone: () -> Unit,
    onMarkCanceled: () -> Unit
) {
    val statusColor = when (appointment.status) {
        AppointmentStatus.DONE -> statusDone
        AppointmentStatus.CANCELED -> statusCanceled
        else -> statusScheduled
    }
    val statusLabel = when (appointment.status) {
        AppointmentStatus.DONE -> stringResource(R.string.appointment_status_done)
        AppointmentStatus.CANCELED -> stringResource(R.string.appointment_status_canceled)
        else -> stringResource(R.string.appointment_status_scheduled)
    }
    var menuExpanded by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = appointment.patientFullName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${minutesToPersianClock(appointment.startMinutes)} · $statusLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = null)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    if (appointment.status == AppointmentStatus.SCHEDULED) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.mark_done)) },
                            leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = statusDone) },
                            onClick = { menuExpanded = false; onMarkDone() }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.mark_canceled)) },
                            leadingIcon = { Icon(Icons.Filled.NotInterested, contentDescription = null) },
                            onClick = { menuExpanded = false; onMarkCanceled() }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { menuExpanded = false; onDelete() }
                    )
                }
            }
        }
    }
}
