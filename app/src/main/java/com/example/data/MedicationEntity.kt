package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "medicamentos")
data class MedicationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val frecuenciaHoras: Int,
    val horaInicio: String = "08:00", // Format "HH:mm"
    val horariosCalculados: String, // Comma-separated list e.g. "08:00,16:00,00:00"
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
) {
    fun getHorariosList(): List<String> {
        return horariosCalculados.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    }
}
