package com.ravanbarvar.patientmanager.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentStatus
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.data.repository.PatientRepository
import com.ravanbarvar.patientmanager.util.JalaliDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository
) : ViewModel() {

    private val _displayedMonth = MutableStateFlow(JalaliDate.today())
    val displayedMonth: StateFlow<JalaliDate> = _displayedMonth.asStateFlow()

    private val _selectedDate = MutableStateFlow(JalaliDate.today())
    val selectedDate: StateFlow<JalaliDate> = _selectedDate.asStateFlow()

    val patients: StateFlow<List<PatientEntity>> = patientRepository.observePatients("")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointmentsForSelectedDate: StateFlow<List<AppointmentWithPatientName>> = _selectedDate
        .flatMapLatest { date -> appointmentRepository.observeForDate(date.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val appointmentDatesInMonth: StateFlow<Set<Long>> = _displayedMonth
        .flatMapLatest { month ->
            val first = JalaliDate(month.year, month.month, 1)
            val start = first.toEpochDay()
            val end = start + first.monthLength() - 1
            appointmentRepository.observeForRange(start, end)
        }
        .map { list -> list.map { it.dateEpochDay }.toSet() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    fun selectDate(date: JalaliDate) {
        _selectedDate.value = date
        if (date.year != _displayedMonth.value.year || date.month != _displayedMonth.value.month) {
            _displayedMonth.value = date
        }
    }

    fun goToNextMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(1)
    }

    fun goToPrevMonth() {
        _displayedMonth.value = _displayedMonth.value.plusMonths(-1)
    }

    fun goToToday() {
        val t = JalaliDate.today()
        _displayedMonth.value = t
        _selectedDate.value = t
    }

    fun addAppointment(patientId: Long, date: JalaliDate, startMinutes: Int, durationMinutes: Int, notes: String) {
        viewModelScope.launch {
            appointmentRepository.addAppointment(
                AppointmentEntity(
                    patientId = patientId,
                    dateEpochDay = date.toEpochDay(),
                    startMinutes = startMinutes,
                    durationMinutes = durationMinutes,
                    notes = notes,
                    status = AppointmentStatus.SCHEDULED,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun updateAppointment(
        original: AppointmentWithPatientName,
        patientId: Long,
        date: JalaliDate,
        startMinutes: Int,
        durationMinutes: Int,
        notes: String
    ) {
        viewModelScope.launch {
            appointmentRepository.updateAppointment(
                original.toEntity().copy(
                    patientId = patientId,
                    dateEpochDay = date.toEpochDay(),
                    startMinutes = startMinutes,
                    durationMinutes = durationMinutes,
                    notes = notes
                )
            )
        }
    }

    fun deleteAppointment(appointment: AppointmentWithPatientName) {
        viewModelScope.launch { appointmentRepository.deleteAppointment(appointment.toEntity()) }
    }

    fun setStatus(appointment: AppointmentWithPatientName, status: String) {
        viewModelScope.launch { appointmentRepository.updateAppointment(appointment.toEntity().copy(status = status)) }
    }
}
