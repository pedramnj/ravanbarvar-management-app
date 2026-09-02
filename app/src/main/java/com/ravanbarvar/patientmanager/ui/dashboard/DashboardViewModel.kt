package com.ravanbarvar.patientmanager.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.data.repository.PatientRepository
import com.ravanbarvar.patientmanager.util.JalaliDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

class DashboardViewModel(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository
) : ViewModel() {

    val today: JalaliDate = JalaliDate.today()

    private val startOfMonth = JalaliDate(today.year, today.month, 1)
    private val startOfMonthMillis = startOfMonth.toLocalDate().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    private val startOfMonthEpoch = startOfMonth.toEpochDay()
    private val endOfMonthEpoch = startOfMonthEpoch + today.monthLength() - 1
    private val startOfWeekEpoch = today.toEpochDay() - today.dayOfWeekIndex()
    private val endOfWeekEpoch = startOfWeekEpoch + 6

    val totalPatients: StateFlow<Int> = patientRepository.observePatientCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val newPatientsThisMonth: StateFlow<Int> = patientRepository.observePatientCountCreatedSince(startOfMonthMillis)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val todayAppointments: StateFlow<List<AppointmentWithPatientName>> = appointmentRepository.observeForDate(today.toEpochDay())
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weekAppointmentsCount: StateFlow<Int> = appointmentRepository.observeForRange(startOfWeekEpoch, endOfWeekEpoch)
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val monthAppointments = appointmentRepository.observeForRange(startOfMonthEpoch, endOfMonthEpoch)

    val monthRevenuePaid: StateFlow<Long> = monthAppointments
        .map { list -> list.filter { it.isPaid }.sumOf { it.feeAmount ?: 0L } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val monthRevenueUnpaid: StateFlow<Long> = monthAppointments
        .map { list -> list.filter { !it.isPaid && it.feeAmount != null }.sumOf { it.feeAmount ?: 0L } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}
