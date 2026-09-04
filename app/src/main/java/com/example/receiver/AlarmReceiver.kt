package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.notification.AlarmScheduler
import com.example.notification.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_TRIGGER_TOMA = "com.example.ACTION_TRIGGER_TOMA"
        const val EXTRA_RECORD_ID = "EXTRA_RECORD_ID"
        const val EXTRA_MEDICATION_ID = "EXTRA_MEDICATION_ID"
        const val EXTRA_MEDICATION_NAME = "EXTRA_MEDICATION_NAME"
        const val EXTRA_SCHEDULED_TIME = "EXTRA_SCHEDULED_TIME"
        const val EXTRA_IS_RETRY = "EXTRA_IS_RETRY"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_TRIGGER_TOMA) return

        val recordId = intent.getLongExtra(EXTRA_RECORD_ID, -1L)
        val medicationId = intent.getLongExtra(EXTRA_MEDICATION_ID, -1L)
        val medicationName = intent.getStringExtra(EXTRA_MEDICATION_NAME) ?: "Medicamento"
        val scheduledTimeText = intent.getStringExtra(EXTRA_SCHEDULED_TIME) ?: ""
        val isRetry = intent.getBooleanExtra(EXTRA_IS_RETRY, false)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val record = db.appDao().getTomaRecordById(recordId)

                // If record exists and is already confirmed, we do NOT show notification or retry
                if (record != null && record.confirmado) {
                    AlarmScheduler.cancelAlarm(context, recordId)
                    return@launch
                }

                val notificationId = (recordId % 10000).toInt() + 1000

                // Show the notification with action button
                NotificationHelper.showMedicationNotification(
                    context = context,
                    notificationId = notificationId,
                    medicationId = medicationId,
                    medicationName = medicationName,
                    scheduledTimeText = scheduledTimeText,
                    recordId = recordId,
                    isInsistentRetry = isRetry
                )

                // Check insistent mode
                val userProfile = db.appDao().getUserProfileOnce()
                val isInsistent = userProfile?.modoInsistente ?: true

                if (isInsistent) {
                    // Schedule a retry reminder in 10 minutes if still unconfirmed
                    AlarmScheduler.scheduleInsistentRetry(
                        context = context,
                        recordId = recordId,
                        medicationId = medicationId,
                        medicationName = medicationName,
                        scheduledTimeText = scheduledTimeText,
                        delayMinutes = 10
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
