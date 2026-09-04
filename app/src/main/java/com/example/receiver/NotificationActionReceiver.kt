package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_CONFIRM_TOMA = "com.example.ACTION_CONFIRM_TOMA"
        const val EXTRA_RECORD_ID = "EXTRA_RECORD_ID"
        const val EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID"
        const val EXTRA_MEDICATION_NAME = "EXTRA_MEDICATION_NAME"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_CONFIRM_TOMA) return

        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medicamento"

        if (notificationId != -1) {
            NotificationHelper.cancelNotification(context, notificationId)
        }

        if (recordId != -1L) {
            // Cancel any retry alarms
            AlarmScheduler.cancelAlarm(context, recordId)

            // Mark as confirmed in Database
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getDatabase(context)
                    db.appDao().setTomaConfirmed(
                        id = recordId,
                        confirmado = true,
                        confirmationTime = System.currentTimeMillis()
                    )
                } finally {
                    pendingResult.finish()
                }
            }

            Toast.makeText(
                context,
                "✓ ¡Toma confirmada: $medicationName!",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
