package com.example.data

import android.content.Context
import com.example.notification.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MedicationRepository(
    private val appDao: AppDao,
    private val context: Context
) {
    val userProfile: Flow<UserProfile?> = appDao.getUserProfile()
    val activeMedications: Flow<List<MedicationEntity>> = appDao.getActiveMedications()
    val allTomaRecords: Flow<List<TomaRecordEntity>> = appDao.getAllTomaRecords()

    fun getTodayTomaRecords(): Flow<List<TomaRecordEntity>> {
        val todayKey = getTodayKey()
        return appDao.getTomaRecordsByDate(todayKey)
    }

    suspend fun saveUserProfile(name: String, photoUri: String?, modoInsistente: Boolean) {
        val current = appDao.getUserProfileOnce()
        val updated = UserProfile(
            id = 1,
            nombre = name,
            fotoPerfil = photoUri ?: current?.fotoPerfil,
            modoInsistente = modoInsistente
        )
        appDao.saveUserProfile(updated)
    }

    suspend fun updateModoInsistente(isInsistent: Boolean) {
        val current = appDao.getUserProfileOnce()
        val updated = current?.copy(modoInsistente = isInsistent)
            ?: UserProfile(id = 1, nombre = "", modoInsistente = isInsistent)
        appDao.saveUserProfile(updated)
    }

    suspend fun addMedication(
        name: String,
        frequencyHours: Int,
        startHourMinute: String = "08:00"
    ): Long {
        val calculatedList = calculateTimes(startHourMinute, frequencyHours)
        val calculatedString = calculatedList.joinToString(",")

        val medication = MedicationEntity(
            nombre = name.trim(),
            frecuenciaHoras = frequencyHours,
            horaInicio = startHourMinute,
            horariosCalculados = calculatedString,
            activo = true
        )
        val medId = appDao.insertMedication(medication)

        // Immediately generate and schedule today's tomas for this medication
        syncTodayTomas()

        return medId
    }

    suspend fun deleteMedication(medicationId: Long) {
        // Cancel alarms for unconfirmed tomas
        val todayKey = getTodayKey()
        val tomas = appDao.getTomaRecordsByDateOnce(todayKey)
        for (toma in tomas) {
            if (toma.medicamentoId == medicationId) {
                AlarmScheduler.cancelAlarm(context, toma.id)
            }
        }
        appDao.deleteRecordsByMedication(medicationId)
        appDao.deleteMedicationById(medicationId)
    }

    suspend fun confirmToma(tomaId: Long) {
        AlarmScheduler.cancelAlarm(context, tomaId)
        appDao.setTomaConfirmed(
            id = tomaId,
            confirmado = true,
            confirmationTime = System.currentTimeMillis()
        )
    }

    suspend fun unconfirmToma(tomaId: Long) {
        appDao.setTomaConfirmed(
            id = tomaId,
            confirmado = false,
            confirmationTime = null
        )
        val record = appDao.getTomaRecordById(tomaId)
        if (record != null && record.fechaHoraProgramada > System.currentTimeMillis()) {
            AlarmScheduler.scheduleMedicationToma(
                context = context,
                recordId = record.id,
                medicationId = record.medicamentoId,
                medicationName = record.medicamentoNombre,
                scheduledTimeText = record.horarioTexto,
                triggerAtMillis = record.fechaHoraProgramada
            )
        }
    }

    /**
     * Generates and syncs today's dose entries for all active medications.
     */
    suspend fun syncTodayTomas() {
        val activeMeds = appDao.getActiveMedicationsOnce()
        val todayKey = getTodayKey()
        val now = System.currentTimeMillis()

        for (med in activeMeds) {
            val times = med.getHorariosList()
            for (timeStr in times) {
                val scheduledTime = computeTimestampForDate(todayKey, timeStr)
                val existing = appDao.findRecordByMedicationAndSchedule(med.id, scheduledTime)

                if (existing == null) {
                    val record = TomaRecordEntity(
                        medicamentoId = med.id,
                        medicamentoNombre = med.nombre,
                        fechaHoraProgramada = scheduledTime,
                        horarioTexto = timeStr,
                        confirmado = false,
                        fechaKey = todayKey
                    )
                    val recordId = appDao.insertTomaRecord(record)

                    // Schedule alarm if in the future
                    if (scheduledTime > now) {
                        AlarmScheduler.scheduleMedicationToma(
                            context = context,
                            recordId = recordId,
                            medicationId = med.id,
                            medicationName = med.nombre,
                            scheduledTimeText = timeStr,
                            triggerAtMillis = scheduledTime
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun getTodayKey(): String {
            return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        fun calculateTimes(startHourMinute: String, frequencyHours: Int): List<String> {
            val parts = startHourMinute.split(":")
            val startHour = parts.getOrNull(0)?.toIntOrNull() ?: 8
            val startMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

            val validFreq = if (frequencyHours <= 0) 8 else frequencyHours
            if (validFreq >= 24) {
                return listOf(String.format(Locale.getDefault(), "%02d:%02d", startHour % 24, startMinute % 60))
            }

            val count = (24 / validFreq).coerceIn(1, 24)
            val result = mutableListOf<String>()

            for (i in 0 until count) {
                val hour = (startHour + i * validFreq) % 24
                result.add(String.format(Locale.getDefault(), "%02d:%02d", hour, startMinute))
            }
            return result
        }

        fun computeTimestampForDate(dateKey: String, timeStr: String): Long {
            val dateParts = dateKey.split("-")
            val timeParts = timeStr.split(":")

            val year = dateParts.getOrNull(0)?.toIntOrNull() ?: 2026
            val month = (dateParts.getOrNull(1)?.toIntOrNull() ?: 1) - 1
            val day = dateParts.getOrNull(2)?.toIntOrNull() ?: 1

            val hour = timeParts.getOrNull(0)?.toIntOrNull() ?: 8
            val minute = timeParts.getOrNull(1)?.toIntOrNull() ?: 0

            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }
    }
}
