package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MedicationEntity
import com.example.ui.components.SeniorButton
import com.example.ui.theme.NaturalAlertRed
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MedicationsListScreen(
    medications: List<MedicationEntity>,
    onAddNewMedication: () -> Unit,
    onDeleteMedication: (Long) -> Unit
) {
    var medToDelete by remember { mutableStateOf<MedicationEntity?>(null) }

    if (medToDelete != null) {
        AlertDialog(
            onDismissRequest = { medToDelete = null },
            title = {
                Text(
                    text = "¿Eliminar medicamento?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Está seguro de que desea eliminar \"${medToDelete!!.nombre}\"? Ya no recibirá recordatorios para este medicamento.",
                    fontSize = 18.sp,
                    color = NaturalTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteMedication(medToDelete!!.id)
                        medToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NaturalAlertRed),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Sí, eliminar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { medToDelete = null }) {
                    Text("Cancelar", fontSize = 16.sp, color = NaturalTextPrimary)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mis Medicamentos",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalPrimary
            )
            Text(
                text = "Lista de medicinas activas y sus frecuencias",
                fontSize = 16.sp,
                color = NaturalTextSecondary
            )
        }

        item {
            // Big Add Medication Button
            SeniorButton(
                text = "+ AGREGAR MEDICAMENTO",
                onClick = onAddNewMedication,
                icon = Icons.Default.Add,
                containerColor = NaturalPrimary,
                testTag = "add_medication_top_button"
            )
        }

        if (medications.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tiene medicamentos cargados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Puede agregarlos escribiendo o usando el botón de voz para dictar su medicina.",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(medications, key = { it.id }) { med ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(24.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = med.nombre,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalTextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { medToDelete = med },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("delete_med_${med.id}_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar ${med.nombre}",
                                    tint = NaturalAlertRed,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Frequency text
                        val freqText = if (med.frecuenciaHoras >= 24) {
                            "Frecuencia: 1 vez al día"
                        } else {
                            "Frecuencia: Cada ${med.frecuenciaHoras} horas (${24 / med.frecuenciaHoras} tomas al día)"
                        }

                        Text(
                            text = freqText,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalPrimary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Calculated times
                        Text(
                            text = "Horarios programados:",
                            fontSize = 15.sp,
                            color = NaturalTextSecondary,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            med.getHorariosList().forEach { time ->
                                Surface(
                                    color = NaturalPrimaryContainer,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.border(1.dp, NaturalBorder, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Schedule,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = NaturalPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "$time hs",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = NaturalPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

