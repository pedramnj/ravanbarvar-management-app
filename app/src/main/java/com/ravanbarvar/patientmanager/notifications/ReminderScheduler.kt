package com.ravanbarvar.patientmanager.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.time.LocalDate
import java.time.ZoneId

/**
 * Schedules a local notification [LEAD_MINUTES] before an appointment's start time via
 * AlarmManager. Uses an exact alarm when the app is allowed to (SCHEDULE_EXACT_ALARM,
 * auto-granted on most devices but user-revocable) — a plain inexact alarm's delivery
 * window can run to 10+ minutes on this OS, which for a 30-minute lead time means the
 * reminder can arrive so late it looks like it never fired. Falls back to the inexact
 * variant only if exact scheduling isn't currently permitted.
 */
object ReminderScheduler {
    const val LEAD_MINUTES = 30L

    private const val EXTRA_APPOINTMENT_ID = "appointment_id"
    private const val EXTRA_PATIENT_NAME = "patient_name"
    private const val EXTRA_TIME_LABEL = "time_label"

    fun triggerTimeMillis(dateEpochDay: Long, startMinutes: Int): Long {
        return LocalDate.ofEpochDay(dateEpochDay)
            .atStartOfDay(ZoneId.systemDefault())
            .plusMinutes(startMinutes.toLong())
            .minusMinutes(LEAD_MINUTES)
            .toInstant()
            .toEpochMilli()
    }

    private fun pendingIntent(context: Context, appointmentId: Long, patientName: String, timeLabel: String): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_APPOINTMENT_ID, appointmentId)
            putExtra(EXTRA_PATIENT_NAME, patientName)
            putExtra(EXTRA_TIME_LABEL, timeLabel)
        }
        return PendingIntent.getBroadcast(
            context,
            appointmentId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun schedule(context: Context, appointmentId: Long, patientName: String, timeLabel: String, triggerAtMillis: Long) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = pendingIntent(context, appointmentId, patientName, timeLabel)
        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()
        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pi)
        }
    }

    fun cancel(context: Context, appointmentId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pi = pendingIntent(context, appointmentId, "", "")
        alarmManager.cancel(pi)
        pi.cancel()
    }
}
