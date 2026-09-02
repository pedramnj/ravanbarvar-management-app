package com.ravanbarvar.patientmanager.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Calendar = "calendar"
    const val Patients = "patients"
    const val Settings = "settings"
    const val PatientDetailPattern = "patient_detail/{patientId}"

    fun patientDetail(patientId: Long) = "patient_detail/$patientId"

    val bottomBarRoutes = setOf(Calendar, Patients, Settings)
}
