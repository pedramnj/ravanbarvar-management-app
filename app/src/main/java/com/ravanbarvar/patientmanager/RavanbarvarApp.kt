package com.ravanbarvar.patientmanager

import android.app.Application
import com.ravanbarvar.patientmanager.data.datastore.SessionManager
import com.ravanbarvar.patientmanager.data.local.AppDatabase
import com.ravanbarvar.patientmanager.data.repository.AppointmentRepository
import com.ravanbarvar.patientmanager.data.repository.AuthRepository
import com.ravanbarvar.patientmanager.data.repository.PatientRepository

class RavanbarvarApp : Application() {

    lateinit var database: AppDatabase
        private set
    lateinit var sessionManager: SessionManager
        private set
    lateinit var authRepository: AuthRepository
        private set
    lateinit var patientRepository: PatientRepository
        private set
    lateinit var appointmentRepository: AppointmentRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getInstance(this)
        sessionManager = SessionManager(this)
        authRepository = AuthRepository(database.adminDao(), sessionManager)
        patientRepository = PatientRepository(database.patientDao(), database.documentDao())
        appointmentRepository = AppointmentRepository(database.appointmentDao())
    }
}
