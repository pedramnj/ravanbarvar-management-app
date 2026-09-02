package com.ravanbarvar.patientmanager.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.ravanbarvar.patientmanager.MainActivity
import com.ravanbarvar.patientmanager.R

object NotificationHelper {
    private const val CHANNEL_ID = "appointment_reminders"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                CHANNEL_ID,
                "یادآوری نوبت‌ها",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "یادآوری پیش از شروع نوبت بیماران"
            }
            manager.createNotificationChannel(channel)
        }
    }

    @SuppressLint("MissingPermission")
    fun showReminder(context: Context, appointmentId: Long, patientName: String, timeLabel: String) {
        ensureChannel(context)

        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) return

        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            appointmentId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("نوبت نزدیک است")
            .setContentText("$patientName ساعت $timeLabel")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(appointmentId.toInt(), notification)
    }
}
