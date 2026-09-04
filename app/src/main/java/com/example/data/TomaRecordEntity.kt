package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros_toma")
data class TomaRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val medicamentoId: Long,
    val medicamentoNombre: String,
    val fechaHoraProgramada: Long, // Epoch timestamp in ms
    val horarioTexto: String, // e.g. "08:00"
    val confirmado: Boolean = false,
    val fechaHoraConfirmacion: Long? = null,
    val fechaKey: String // Format "yyyy-MM-dd" for quick grouping
)
