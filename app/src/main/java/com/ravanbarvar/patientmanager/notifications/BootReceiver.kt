package com.ravanbarvar.patientmanager.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ravanbarvar.patientmanager.data.datastore.SessionManager
import com.ravanbarvar.patientmanager.data.local.AppDatabase
import com.ravanbarvar.patientmanager.util.minutesToPersianClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * AlarmManager alarms don't survive a reboot, so every future scheduled appointment's
 * reminder needs to be re-armed once the device (and this app) comes back up.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val sessionManager = SessionManager(appContext)
                if (sessionManager.remindersEnabled.first()) {
                    val db = AppDatabase.getInstance(appContext)
                    val todayEpoch = LocalDate.now().toEpochDay()
                    val nowMillis = System.currentTimeMillis()
                    db.appointmentDao().getScheduledFrom(todayEpoch).forEach { appt ->
                        val triggerAt = ReminderScheduler.triggerTimeMillis(appt.dateEpochDay, appt.startMinutes)
                        if (triggerAt > nowMillis) {
                            ReminderScheduler.schedule(
                                appContext,
                                appt.id,
                                appt.patientFullName,
                                minutesToPersianClock(appt.startMinutes),
                                triggerAt
                            )
                        }
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
