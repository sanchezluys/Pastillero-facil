package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.MedicationEntity
import com.example.data.MedicationRepository
import com.example.data.TomaRecordEntity
import com.example.data.UserProfile
import com.example.notification.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MedicationRepository(db.appDao(), application)

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMedications: StateFlow<List<MedicationEntity>> = repository.activeMedications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTomas: StateFlow<List<TomaRecordEntity>> = repository.getTodayTomaRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTomasHistory: StateFlow<List<TomaRecordEntity>> = repository.allTomaRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedTab = MutableStateFlow(0) // 0: Hoy, 1: Medicamentos, 2: Historial, 3: Perfil
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    init {
        // Automatically sync today's doses upon launch
        viewModelScope.launch {
            repository.syncTodayTomas()
        }
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun saveUserProfile(name: String, photoUri: String?, modoInsistente: Boolean, notifyUser: Boolean = false) {
        viewModelScope.launch {
            repository.saveUserProfile(name, photoUri, modoInsistente)
            if (notifyUser) {
                _userMessage.value = "Perfil guardado con éxito"
            }
        }
    }

    fun updateModoInsistente(isInsistent: Boolean) {
        viewModelScope.launch {
            repository.updateModoInsistente(isInsistent)
            _userMessage.value = if (isInsistent) {
                "Modo insistente activado (reintentará avisos)"
            } else {
                "Modo simple activado (notifica una sola vez)"
            }
        }
    }

    fun addMedication(name: String, frequencyHours: Int, startHourMinute: String) {
        viewModelScope.launch {
            val id = repository.addMedication(name, frequencyHours, startHourMinute)
            if (id > 0) {
                _userMessage.value = "✓ Medicamento '$name' guardado correctamente"
            }
        }
    }

    fun deleteMedication(medicationId: Long) {
        viewModelScope.launch {
            repository.deleteMedication(medicationId)
            _userMessage.value = "Medicamento eliminado"
        }
    }

    fun confirmToma(recordId: Long) {
        viewModelScope.launch {
            repository.confirmToma(recordId)
            _userMessage.value = "✓ ¡Excelente! Toma confirmada registrada"
        }
    }

    fun unconfirmToma(recordId: Long) {
        viewModelScope.launch {
            repository.unconfirmToma(recordId)
            _userMessage.value = "Toma desmarcada"
        }
    }

    fun refreshToday() {
        viewModelScope.launch {
            repository.syncTodayTomas()
        }
    }

    fun testReminderNotification() {
        NotificationHelper.showMedicationNotification(
            context = getApplication(),
            notificationId = 9999,
            medicationId = 0L,
            medicationName = "Medicamento de Prueba",
            scheduledTimeText = "Ahora",
            recordId = 0L,
            isInsistentRetry = false
        )
        _userMessage.value = "🔔 Alarma de prueba enviada a la barra de notificaciones"
    }
}
