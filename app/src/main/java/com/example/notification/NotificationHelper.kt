package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_ID = "pastillero_recordatorios_channel"
    const val CHANNEL_NAME = "Recordatorio de Medicamentos"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones y recordatorios para la toma de medicamentos"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setSound(soundUri, null)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showMedicationNotification(
        context: Context,
        notificationId: Int,
        medicationId: Long,
        medicationName: String,
        scheduledTimeText: String,
        recordId: Long,
        isInsistentRetry: Boolean = false
    ) {
        createNotificationChannel(context)

        // Main app launch intent
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("EXTRA_MEDICATION_ID", medicationId)
            putExtra("EXTRA_RECORD_ID", recordId)
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Confirm Dose
        val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_CONFIRM_TOMA
            putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            putExtra(NotificationActionReceiver.EXTRA_RECORD_ID, recordId)
            putExtra(NotificationActionReceiver.EXTRA_MEDICATION_NAME, medicationName)
        }
        val confirmPendingIntent = PendingIntent.getBroadcast(
            context,
            notificationId,
            confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = if (isInsistentRetry) {
            "⚠️ ¡Aviso importante! No olvide su medicina"
        } else {
            "💊 ¡Hora de su medicamento!"
        }

        val bodyText = if (isInsistentRetry) {
            "Aún no ha confirmado su toma de: $medicationName ($scheduledTimeText). Por favor tómela ahora."
        } else {
            "Es momento de tomar: $medicationName programado a las $scheduledTimeText."
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(bodyText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bodyText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(openPendingIntent)
            .setAutoCancel(true)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "¡YA LA TOMÉ!",
                confirmPendingIntent
            )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    fun cancelNotification(context: Context, notificationId: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(notificationId)
    }
}
