package com.ravanbarvar.patientmanager.data.repository

import com.ravanbarvar.patientmanager.data.local.dao.DocumentDao
import com.ravanbarvar.patientmanager.data.local.dao.PatientDao
import com.ravanbarvar.patientmanager.data.local.entity.DocumentEntity
import com.ravanbarvar.patientmanager.data.local.entity.PatientEntity
import kotlinx.coroutines.flow.Flow

class PatientRepository(
    private val patientDao: PatientDao,
    private val documentDao: DocumentDao
) {
    fun observePatients(query: String): Flow<List<PatientEntity>> =
        if (query.isBlank()) patientDao.observeAll() else patientDao.search(query.trim())

    fun observePatient(id: Long): Flow<PatientEntity?> = patientDao.observeById(id)

    suspend fun getPatient(id: Long): PatientEntity? = patientDao.getById(id)

    fun observePatientCount(): Flow<Int> = patientDao.observeCount()

    suspend fun addPatient(patient: PatientEntity): Long = patientDao.insert(patient)

    suspend fun updatePatient(patient: PatientEntity) = patientDao.update(patient)

    suspend fun deletePatient(patient: PatientEntity) = patientDao.delete(patient)

    fun observeDocuments(patientId: Long): Flow<List<DocumentEntity>> =
        documentDao.observeForPatient(patientId)

    suspend fun addDocument(document: DocumentEntity): Long = documentDao.insert(document)

    suspend fun deleteDocument(document: DocumentEntity) = documentDao.delete(document)
}
