package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.receiver.AlarmReceiver

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleMedicationToma(
        context: Context,
        recordId: Long,
        medicationId: Long,
        medicationName: String,
        scheduledTimeText: String,
        triggerAtMillis: Long,
        isInsistentRetry: Boolean = false
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // If scheduled time is in past, don't schedule standard past alarm unless it's an immediate retry
        if (triggerAtMillis <= System.currentTimeMillis() && !isInsistentRetry) {
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_TOMA
            putExtra(AlarmReceiver.EXTRA_RECORD_ID, recordId)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_ID, medicationId)
            putExtra(AlarmReceiver.EXTRA_MEDICATION_NAME, medicationName)
            putExtra(AlarmReceiver.EXTRA_SCHEDULED_TIME, scheduledTimeText)
            putExtra(AlarmReceiver.EXTRA_IS_RETRY, isInsistentRetry)
        }

        val requestCode = (recordId xor (if (isInsistentRetry) 0x7FFF else 0)).toInt()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for $medicationName at $triggerAtMillis (recordId: $recordId, retry: $isInsistentRetry)")
        } catch (e: SecurityException) {
            Log.w(TAG, "Exact alarm permission not granted, using fallback", e)
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error scheduling alarm", e)
        }
    }

    fun scheduleInsistentRetry(
        context: Context,
        recordId: Long,
        medicationId: Long,
        medicationName: String,
        scheduledTimeText: String,
        delayMinutes: Int = 10
    ) {
        val triggerAt = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)
        scheduleMedicationToma(
            context = context,
            recordId = recordId,
            medicationId = medicationId,
            medicationName = medicationName,
            scheduledTimeText = scheduledTimeText,
            triggerAtMillis = triggerAt,
            isInsistentRetry = true
        )
    }

    fun cancelAlarm(context: Context, recordId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_TRIGGER_TOMA
        }
        for (isRetry in listOf(false, true)) {
            val requestCode = (recordId xor (if (isRetry) 0x7FFF else 0)).toInt()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}
