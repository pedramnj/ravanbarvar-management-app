package com.ravanbarvar.patientmanager.ui.patients

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ravanbarvar.patientmanager.data.local.entity.Gender
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import com.ravanbarvar.patientmanager.data.repository.PatientRepository
import com.ravanbarvar.patientmanager.util.ContactSummary
import com.ravanbarvar.patientmanager.util.ContactsUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImportContactsViewModel(
    private val patientRepository: PatientRepository,
    private val appContext: Context
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set
    var contacts by mutableStateOf<List<ContactSummary>>(emptyList())
        private set
    var query by mutableStateOf("")
    var selectedIds by mutableStateOf<Set<Long>>(emptySet())
        private set
    var isImporting by mutableStateOf(false)
        private set
    private var loaded = false

    val filteredContacts: List<ContactSummary>
        get() = if (query.isBlank()) {
            contacts
        } else {
            contacts.filter { it.name.contains(query, ignoreCase = true) || it.phone.contains(query) }
        }

    fun load() {
        if (loaded || isLoading) return
        loaded = true
        viewModelScope.launch {
            isLoading = true
            val existingPhones = patientRepository.observePatients("").first()
                .mapNotNull { it.phone.takeIf(String::isNotBlank) }
                .toSet()
            val fetched = withContext(Dispatchers.IO) { ContactsUtils.queryContacts(appContext) }
            contacts = fetched.filter { it.phone.isBlank() || it.phone !in existingPhones }
            isLoading = false
        }
    }

    fun toggle(id: Long) {
        selectedIds = if (id in selectedIds) selectedIds - id else selectedIds + id
    }

    fun selectAll() {
        selectedIds = selectedIds + filteredContacts.map { it.id }
    }

    fun clearSelection() {
        selectedIds = emptySet()
    }

    fun importSelected(onDone: (Int) -> Unit) {
        val toImport = contacts.filter { it.id in selectedIds }
        if (toImport.isEmpty()) return
        viewModelScope.launch {
            isImporting = true
            val now = System.currentTimeMillis()
            toImport.forEach { contact ->
                val (first, last) = ContactsUtils.splitName(contact.name)
                patientRepository.addPatient(
                    PatientEntity(
                        firstName = first,
                        lastName = last,
                        phone = contact.phone,
                        nationalId = "",
                        birthDateEpochDay = null,
                        gender = Gender.OTHER,
                        notes = "",
                        createdAt = now,
                        updatedAt = now
                    )
                )
            }
            isImporting = false
            onDone(toImport.size)
        }
    }
}
