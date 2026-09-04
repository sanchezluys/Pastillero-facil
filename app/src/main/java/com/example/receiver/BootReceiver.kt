package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val tomas = db.appDao().getTomaRecordsByDateOnce(todayKey)
                val now = System.currentTimeMillis()

                for (toma in tomas) {
                    if (!toma.confirmado && toma.fechaHoraProgramada > now) {
                        AlarmScheduler.scheduleMedicationToma(
                            context = context,
                            recordId = toma.id,
                            medicationId = toma.medicamentoId,
                            medicationName = toma.medicamentoNombre,
                            scheduledTimeText = toma.horarioTexto,
                            triggerAtMillis = toma.fechaHoraProgramada
                        )
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
