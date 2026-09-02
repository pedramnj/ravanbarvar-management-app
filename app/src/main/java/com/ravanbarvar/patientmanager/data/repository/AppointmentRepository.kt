package com.ravanbarvar.patientmanager.data.repository

import com.ravanbarvar.patientmanager.data.local.dao.AppointmentDao
import com.ravanbarvar.patientmanager.data.local.dao.AppointmentWithPatientName
import com.ravanbarvar.patientmanager.data.local.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

class AppointmentRepository(private val appointmentDao: AppointmentDao) {

    fun observeForDate(epochDay: Long): Flow<List<AppointmentWithPatientName>> =
        appointmentDao.observeForDate(epochDay)

    fun observeForRange(startEpochDay: Long, endEpochDay: Long): Flow<List<AppointmentWithPatientName>> =
        appointmentDao.observeForRange(startEpochDay, endEpochDay)

    fun observeForPatient(patientId: Long): Flow<List<AppointmentWithPatientName>> =
        appointmentDao.observeForPatient(patientId)

    fun observeNextUpcomingForPatient(patientId: Long, todayEpochDay: Long, nowMinutes: Int): Flow<AppointmentWithPatientName?> =
        appointmentDao.observeNextUpcomingForPatient(patientId, todayEpochDay, nowMinutes)

    suspend fun addAppointment(appointment: AppointmentEntity): Long = appointmentDao.insert(appointment)

    suspend fun updateAppointment(appointment: AppointmentEntity) = appointmentDao.update(appointment)

    suspend fun deleteAppointment(appointment: AppointmentEntity) = appointmentDao.delete(appointment)

    suspend fun getById(id: Long): AppointmentEntity? = appointmentDao.getById(id)
}
