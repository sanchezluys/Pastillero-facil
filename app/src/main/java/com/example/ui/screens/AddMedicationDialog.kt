package com.example.ui.screens

import android.app.Activity
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MedicationRepository
import com.example.ui.components.SeniorButton
import com.example.ui.components.SeniorOutlinedButton
import com.example.ui.theme.NaturalActionCardBackground
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary
import com.example.voice.VoiceInputHelper

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMedicationDialog(
    onDismiss: () -> Unit,
    onSave: (name: String, frequencyHours: Int, startHourMinute: String) -> Unit
) {
    val context = LocalContext.current
    var medName by remember { mutableStateOf("") }
    var frequencyHours by remember { mutableIntStateOf(8) } // Default: every 8 hours
    var startHour by remember { mutableIntStateOf(8) } // Default: 08:00 AM
    var startMinute by remember { mutableIntStateOf(0) }
    var voiceMessage by remember { mutableStateOf<String?>(null) }

    // Speech Recognition Launcher
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val parsed = VoiceInputHelper.parseMedicationVoice(spoken)
                if (parsed.name.isNotBlank()) {
                    medName = parsed.name
                }
                if (parsed.frequencyHours != null) {
                    frequencyHours = parsed.frequencyHours
                }
                voiceMessage = "Escuchado: \"$spoken\""
                Toast.makeText(context, "Cargado por voz: ${parsed.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val calculatedTimes = remember(startHour, startMinute, frequencyHours) {
        val startFormatted = String.format("%02d:%02d", startHour, startMinute)
        MedicationRepository.calculateTimes(startFormatted, frequencyHours)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Agregar Medicamento",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_add_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = NaturalTextPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Voice input card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalActionCardBackground),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "¿Prefiere hablar en vez de escribir?",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Diga el nombre y cada cuántas horas (ej: \"Ibuprofeno cada 8 horas\")",
                            fontSize = 15.sp,
                            color = NaturalTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        SeniorButton(
                            text = "🎙️ HABLAR PARA CARGAR",
                            onClick = {
                                try {
                                    val intent = VoiceInputHelper.createSpeechIntent()
                                    speechLauncher.launch(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(
                                        context,
                                        "El reconocimiento de voz no está disponible en este dispositivo",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            containerColor = NaturalPrimary,
                            testTag = "voice_input_button"
                        )
                        if (voiceMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = voiceMessage!!,
                                fontSize = 14.sp,
                                color = NaturalPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Name field
                Text(
                    text = "1. Nombre del medicamento:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    placeholder = { Text("Ej: Losartán 50mg", fontSize = 18.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("med_name_input"),
                    shape = RoundedCornerShape(16.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NaturalPrimary,
                        unfocusedBorderColor = NaturalBorder
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Frequency selection
                Text(
                    text = "2. ¿Cada cuántas horas debe tomarlo?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Spacer(modifier = Modifier.height(10.dp))

                val presetFrequencies = listOf(
                    4 to "Cada 4 hs",
                    6 to "Cada 6 hs",
                    8 to "Cada 8 hs",
                    12 to "Cada 12 hs",
                    24 to "1 vez al día (24 hs)"
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    presetFrequencies.forEach { (freq, label) ->
                        val isSelected = frequencyHours == freq
                        Surface(
                            modifier = Modifier
                                .clickable { frequencyHours = freq }
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) NaturalPrimary else NaturalBorder,
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .testTag("freq_${freq}_button"),
                            color = if (isSelected) NaturalPrimaryContainer else Color(0xFFF1F0F4),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = label,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) NaturalPrimary else NaturalTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Starting hour (first dose)
                Text(
                    text = "3. Hora de la primera toma:",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = {
                            startHour = if (startHour == 0) 23 else startHour - 1
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFEEF0F7), RoundedCornerShape(16.dp))
                            .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                            .testTag("hour_minus_button")
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Menos una hora", modifier = Modifier.size(28.dp), tint = NaturalPrimary)
                    }

                    Surface(
                        modifier = Modifier
                            .border(2.dp, NaturalPrimary, RoundedCornerShape(18.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        color = Color.White
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = NaturalPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = String.format("%02d:%02d hs", startHour, startMinute),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            startHour = (startHour + 1) % 24
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .background(Color(0xFFEEF0F7), RoundedCornerShape(16.dp))
                            .border(1.dp, NaturalBorder, RoundedCornerShape(16.dp))
                            .testTag("hour_plus_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Más una hora", modifier = Modifier.size(28.dp), tint = NaturalPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Calculated times card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(20.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalPrimaryContainer),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "⏰ Horarios calculados automáticamente:",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            calculatedTimes.forEach { timeStr ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                                ) {
                                    Text(
                                        text = "$timeStr hs",
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NaturalPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save button
                SeniorButton(
                    text = "GUARDAR MEDICAMENTO",
                    onClick = {
                        if (medName.isBlank()) {
                            Toast.makeText(context, "Por favor ingrese el nombre del medicamento", Toast.LENGTH_SHORT).show()
                        } else {
                            val startFormatted = String.format("%02d:%02d", startHour, startMinute)
                            onSave(medName, frequencyHours, startFormatted)
                            onDismiss()
                        }
                    },
                    containerColor = NaturalPrimary,
                    testTag = "save_medication_button"
                )

                Spacer(modifier = Modifier.height(12.dp))

                SeniorOutlinedButton(
                    text = "Cancelar",
                    onClick = onDismiss,
                    contentColor = NaturalPrimary,
                    testTag = "cancel_medication_button"
                )
            }
        }
    }
}

