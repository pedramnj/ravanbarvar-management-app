package com.ravanbarvar.patientmanager.ui.navigation

object Routes {
    const val Splash = "splash"
    const val Login = "login"
    const val Dashboard = "dashboard"
    const val Calendar = "calendar"
    const val Patients = "patients"
    const val Settings = "settings"
    const val ImportContacts = "import_contacts"
    const val PatientDetailPattern = "patient_detail/{patientId}"

    fun patientDetail(patientId: Long) = "patient_detail/$patientId"

    val bottomBarRoutes = setOf(Dashboard, Calendar, Patients, Settings)
}
