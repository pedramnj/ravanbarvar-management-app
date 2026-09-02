package com.ravanbarvar.patientmanager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appointmentId = intent.getLongExtra("appointment_id", -1L)
        val patientName = intent.getStringExtra("patient_name")
        val timeLabel = intent.getStringExtra("time_label")
        if (appointmentId <= 0 || patientName.isNullOrBlank() || timeLabel.isNullOrBlank()) return
        NotificationHelper.showReminder(context, appointmentId, patientName, timeLabel)
    }
}
