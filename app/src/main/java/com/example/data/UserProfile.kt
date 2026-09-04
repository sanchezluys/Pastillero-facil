package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val nombre: String = "",
    val fotoPerfil: String? = null,
    val modoInsistente: Boolean = true
)
