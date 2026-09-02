package com.ravanbarvar.patientmanager.ui.calendar

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.datastore.SessionManager
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentStatus
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.data.repository.PatientRepository
import com.ravanbarvar.patientmanager.notifications.ReminderScheduler
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.minutesToPersianClock
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val appointmentRepository: AppointmentRepository,
    private val patientRepository: PatientRepository,
    private val sessionManager: SessionManager,
    private val appContext: Context
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

    /** Flat upcoming list for the agenda view: today through the next 60 days. */
    val agendaAppointments: StateFlow<List<AppointmentWithPatientName>> = run {
        val today = JalaliDate.today().toEpochDay()
        appointmentRepository.observeForRange(today, today + 60)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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

    fun addAppointment(
        patientId: Long,
        date: JalaliDate,
        startMinutes: Int,
        durationMinutes: Int,
        notes: String,
        feeAmount: Long?,
        isPaid: Boolean
    ) {
        viewModelScope.launch {
            val entity = AppointmentEntity(
                patientId = patientId,
                dateEpochDay = date.toEpochDay(),
                startMinutes = startMinutes,
                durationMinutes = durationMinutes,
                notes = notes,
                status = AppointmentStatus.SCHEDULED,
                createdAt = System.currentTimeMillis(),
                feeAmount = feeAmount,
                isPaid = isPaid
            )
            val id = appointmentRepository.addAppointment(entity)
            syncReminder(entity.copy(id = id), patientId)
        }
    }

    fun updateAppointment(
        original: AppointmentWithPatientName,
        patientId: Long,
        date: JalaliDate,
        startMinutes: Int,
        durationMinutes: Int,
        notes: String,
        feeAmount: Long?,
        isPaid: Boolean
    ) {
        viewModelScope.launch {
            val entity = original.toEntity().copy(
                patientId = patientId,
                dateEpochDay = date.toEpochDay(),
                startMinutes = startMinutes,
                durationMinutes = durationMinutes,
                notes = notes,
                feeAmount = feeAmount,
                isPaid = isPaid
            )
            appointmentRepository.updateAppointment(entity)
            syncReminder(entity, patientId)
        }
    }

    fun deleteAppointment(appointment: AppointmentWithPatientName) {
        viewModelScope.launch {
            appointmentRepository.deleteAppointment(appointment.toEntity())
            ReminderScheduler.cancel(appContext, appointment.id)
        }
    }

    fun setStatus(appointment: AppointmentWithPatientName, status: String) {
        viewModelScope.launch {
            appointmentRepository.updateAppointment(appointment.toEntity().copy(status = status))
            ReminderScheduler.cancel(appContext, appointment.id)
        }
    }

    fun setPaid(appointment: AppointmentWithPatientName, isPaid: Boolean) {
        viewModelScope.launch {
            appointmentRepository.updateAppointment(appointment.toEntity().copy(isPaid = isPaid))
        }
    }

    private suspend fun syncReminder(appointment: AppointmentEntity, patientId: Long) {
        if (appointment.status != AppointmentStatus.SCHEDULED || !sessionManager.remindersEnabled.first()) {
            ReminderScheduler.cancel(appContext, appointment.id)
            return
        }
        val patientName = patients.value.firstOrNull { it.id == patientId }
            ?.let { "${it.firstName} ${it.lastName}" }
            ?: return
        val triggerAt = ReminderScheduler.triggerTimeMillis(appointment.dateEpochDay, appointment.startMinutes)
        ReminderScheduler.schedule(appContext, appointment.id, patientName, minutesToPersianClock(appointment.startMinutes), triggerAt)
    }
}
