package com.ravanbarvar.patientmanager.ui.patients

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import com.ravanbarvar.patientmanager.data.local.entity.Gender
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.data.repository.PatientRepository
import com.ravanbarvar.patientmanager.util.JalaliDate
import com.ravanbarvar.patientmanager.util.currentMinutesOfDay
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PatientDetailViewModel(
    private val patientRepository: PatientRepository,
    private val appointmentRepository: AppointmentRepository,
    initialPatientId: Long
) : ViewModel() {

    var patientId by mutableStateOf(if (initialPatientId > 0) initialPatientId else null)
        private set

    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var phone by mutableStateOf("")
    var nationalId by mutableStateOf("")
    var gender by mutableStateOf(Gender.MALE)
    var notes by mutableStateOf("")
    var birthDate by mutableStateOf<JalaliDate?>(null)
    var photoUri by mutableStateOf<String?>(null)
    var isLoaded by mutableStateOf(initialPatientId <= 0)
        private set
    var isSaving by mutableStateOf(false)
        private set
    var saveError by mutableStateOf<String?>(null)
        private set

    private var createdAt: Long = System.currentTimeMillis()

    val isNew: Boolean get() = patientId == null

    init {
        val id = patientId
        if (id != null) {
            viewModelScope.launch {
                val p = patientRepository.getPatient(id)
                if (p != null) {
                    firstName = p.firstName
                    lastName = p.lastName
                    phone = p.phone
                    nationalId = p.nationalId
                    gender = p.gender
                    notes = p.notes
                    birthDate = p.birthDateEpochDay?.let { JalaliDate.fromEpochDay(it) }
                    photoUri = p.photoUri
                    createdAt = p.createdAt
                }
                isLoaded = true
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val documents: StateFlow<List<DocumentEntity>> = snapshotFlow { patientId }
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else patientRepository.observeDocuments(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val upcomingAppointment: StateFlow<AppointmentWithPatientName?> = snapshotFlow { patientId }
        .flatMapLatest { id ->
            if (id == null) {
                flowOf(null)
            } else {
                val today = JalaliDate.today()
                appointmentRepository.observeNextUpcomingForPatient(id, today.toEpochDay(), currentMinutesOfDay())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val sessionHistory: StateFlow<List<AppointmentWithPatientName>> = snapshotFlow { patientId }
        .flatMapLatest { id -> if (id == null) flowOf(emptyList()) else appointmentRepository.observeForPatient(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFieldsChanged() {
        saveError = null
    }

    fun save(requiredFieldError: String, onSaved: (Long) -> Unit) {
        if (firstName.isBlank() || lastName.isBlank()) {
            saveError = requiredFieldError
            return
        }
        viewModelScope.launch {
            isSaving = true
            val id = patientId
            val now = System.currentTimeMillis()
            if (id == null) {
                val newId = patientRepository.addPatient(
                    PatientEntity(
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim(),
                        nationalId = nationalId.trim(),
                        birthDateEpochDay = birthDate?.toEpochDay(),
                        gender = gender,
                        notes = notes,
                        createdAt = now,
                        updatedAt = now,
                        photoUri = photoUri
                    )
                )
                createdAt = now
                patientId = newId
                isSaving = false
                onSaved(newId)
            } else {
                patientRepository.updatePatient(
                    PatientEntity(
                        id = id,
                        firstName = firstName.trim(),
                        lastName = lastName.trim(),
                        phone = phone.trim(),
                        nationalId = nationalId.trim(),
                        birthDateEpochDay = birthDate?.toEpochDay(),
                        gender = gender,
                        notes = notes,
                        createdAt = createdAt,
                        updatedAt = now,
                        photoUri = photoUri
                    )
                )
                isSaving = false
                onSaved(id)
            }
        }
    }

    fun deletePatient(onDeleted: () -> Unit) {
        val id = patientId ?: return
        viewModelScope.launch {
            val p = patientRepository.getPatient(id) ?: return@launch
            patientRepository.deletePatient(p)
            onDeleted()
        }
    }

    fun addDocument(uri: String, displayName: String, mimeType: String) {
        val id = patientId ?: return
        viewModelScope.launch {
            patientRepository.addDocument(
                DocumentEntity(
                    patientId = id,
                    uri = uri,
                    displayName = displayName,
                    mimeType = mimeType,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteDocument(document: DocumentEntity) {
        viewModelScope.launch { patientRepository.deleteDocument(document) }
    }
}
