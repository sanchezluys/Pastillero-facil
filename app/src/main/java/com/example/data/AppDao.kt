package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- User Profile ---
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    // --- Medications ---
    @Query("SELECT * FROM medicamentos WHERE activo = 1 ORDER BY id DESC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>

    @Query("SELECT * FROM medicamentos WHERE activo = 1")
    suspend fun getActiveMedicationsOnce(): List<MedicationEntity>

    @Query("SELECT * FROM medicamentos WHERE id = :id LIMIT 1")
    suspend fun getMedicationById(id: Long): MedicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long

    @Update
    suspend fun updateMedication(medication: MedicationEntity)

    @Query("DELETE FROM medicamentos WHERE id = :id")
    suspend fun deleteMedicationById(id: Long)

    // --- Toma Records ---
    @Query("SELECT * FROM registros_toma WHERE fechaKey = :dateKey ORDER BY fechaHoraProgramada ASC")
    fun getTomaRecordsByDate(dateKey: String): Flow<List<TomaRecordEntity>>

    @Query("SELECT * FROM registros_toma WHERE fechaKey = :dateKey ORDER BY fechaHoraProgramada ASC")
    suspend fun getTomaRecordsByDateOnce(dateKey: String): List<TomaRecordEntity>

    @Query("SELECT * FROM registros_toma ORDER BY fechaHoraProgramada DESC")
    fun getAllTomaRecords(): Flow<List<TomaRecordEntity>>

    @Query("SELECT * FROM registros_toma WHERE id = :id LIMIT 1")
    suspend fun getTomaRecordById(id: Long): TomaRecordEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTomaRecords(records: List<TomaRecordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTomaRecord(record: TomaRecordEntity): Long

    @Query("UPDATE registros_toma SET confirmado = :confirmado, fechaHoraConfirmacion = :confirmationTime WHERE id = :id")
    suspend fun setTomaConfirmed(id: Long, confirmado: Boolean, confirmationTime: Long?)

    @Query("SELECT * FROM registros_toma WHERE medicamentoId = :medicationId AND fechaHoraProgramada = :scheduledTime LIMIT 1")
    suspend fun findRecordByMedicationAndSchedule(medicationId: Long, scheduledTime: Long): TomaRecordEntity?

    @Query("DELETE FROM registros_toma WHERE medicamentoId = :medicationId")
    suspend fun deleteRecordsByMedication(medicationId: Long)
}
