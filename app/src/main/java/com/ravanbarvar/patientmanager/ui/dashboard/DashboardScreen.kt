package com.ravanbarvar.patientmanager.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonAddAlt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ravanbarvar.patientmanager.R
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.ui.currentApp
import com.ravanbarvar.patientmanager.ui.theme.LavenderSecondary
import com.ravanbarvar.patientmanager.ui.theme.SagePrimary
import com.ravanbarvar.patientmanager.ui.theme.SuccessGreen
import com.ravanbarvar.patientmanager.ui.theme.WarningAmber
import com.ravanbarvar.patientmanager.util.PersianDigits
import com.ravanbarvar.patientmanager.util.formatToman
import com.ravanbarvar.patientmanager.util.minutesToPersianClock
import java.time.LocalTime

@Composable
fun DashboardScreen(onAddPatient: () -> Unit, onOpenCalendar: () -> Unit) {
    val app = currentApp()
    val viewModel: DashboardViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DashboardViewModel(app.patientRepository, app.appointmentRepository) }
        }
    )

    val totalPatients by viewModel.totalPatients.collectAsState()
    val newPatientsThisMonth by viewModel.newPatientsThisMonth.collectAsState()
    val todayAppointments by viewModel.todayAppointments.collectAsState()
    val weekAppointmentsCount by viewModel.weekAppointmentsCount.collectAsState()
    val monthRevenuePaid by viewModel.monthRevenuePaid.collectAsState()
    val monthRevenueUnpaid by viewModel.monthRevenueUnpaid.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = greeting(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = viewModel.today.formatted(),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                icon = Icons.Filled.Groups,
                tint = SagePrimary,
                value = PersianDigits.of(totalPatients),
                label = stringResource(R.string.stat_total_patients),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.PersonAddAlt,
                tint = LavenderSecondary,
                value = PersianDigits.of(newPatientsThisMonth),
                label = stringResource(R.string.stat_new_patients_month),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                icon = Icons.Filled.EventAvailable,
                tint = SagePrimary,
                value = PersianDigits.of(todayAppointments.size),
                label = stringResource(R.string.stat_today_appointments),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.CalendarMonth,
                tint = LavenderSecondary,
                value = PersianDigits.of(weekAppointmentsCount),
                label = stringResource(R.string.stat_week_appointments),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            StatCard(
                icon = Icons.Filled.Savings,
                tint = SuccessGreen,
                value = formatToman(monthRevenuePaid),
                label = stringResource(R.string.stat_revenue_month),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                icon = Icons.Filled.Warning,
                tint = WarningAmber,
                value = formatToman(monthRevenueUnpaid),
                label = stringResource(R.string.stat_outstanding),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onAddPatient,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.add_patient))
            }
            OutlinedButton(
                onClick = onOpenCalendar,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(stringResource(R.string.add_appointment))
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.stat_today_appointments),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(10.dp))

        if (todayAppointments.isEmpty()) {
            Text(
                text = stringResource(R.string.no_appointments),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 6.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                todayAppointments.forEach { appt -> TodayAppointmentRow(appt, onOpenCalendar) }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

private fun greeting(): String {
    val hour = LocalTime.now().hour
    return when {
        hour < 12 -> "صبح بخیر"
        hour < 17 -> "ظهر بخیر"
        hour < 20 -> "عصر بخیر"
        else -> "شب بخیر"
    }
}

@Composable
private fun StatCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(tint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodayAppointmentRow(appointment: AppointmentWithPatientName, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = SagePrimary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(
                text = appointment.patientFullName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = minutesToPersianClock(appointment.startMinutes),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
