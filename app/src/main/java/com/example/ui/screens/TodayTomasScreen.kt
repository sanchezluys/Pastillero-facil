package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TomaRecordEntity
import com.example.data.UserProfile
import com.example.ui.components.SeniorButton
import com.example.ui.components.SeniorCard
import com.example.ui.components.SeniorStatusBadge
import com.example.ui.theme.NaturalActionCardBackground
import com.example.ui.theme.NaturalAlertContainer
import com.example.ui.theme.NaturalAlertRed
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalCardWhite
import com.example.ui.theme.NaturalHeroCardBackground
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalSuccessGreen
import com.example.ui.theme.NaturalSuccessGreenContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TodayTomasScreen(
    userProfile: UserProfile?,
    todayTomas: List<TomaRecordEntity>,
    onConfirmToma: (Long) -> Unit,
    onUnconfirmToma: (Long) -> Unit,
    onNavigateToAddMedication: () -> Unit,
    onRefresh: () -> Unit
) {
    val totalTomas = todayTomas.size
    val confirmedCount = todayTomas.count { it.confirmado }
    val progress = if (totalTomas > 0) confirmedCount.toFloat() / totalTomas else 0f

    // Find next pending toma
    val nextPendingToma = remember(todayTomas) {
        todayTomas.firstOrNull { !it.confirmado }
    }

    // Check for overdue toma (pending and scheduled time is already passed by > 15 mins)
    val now = System.currentTimeMillis()
    val overdueToma = remember(todayTomas, now) {
        todayTomas.firstOrNull { !it.confirmado && it.fechaHoraProgramada < now - (15 * 60 * 1000) }
    }

    val greetingName = if (!userProfile?.nombre.isNullOrBlank()) {
        "Hola, ${userProfile!!.nombre}"
    } else {
        "Hola, Carmen"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NaturalBackground)
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Greeting Header matching Natural Tones style
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = greetingName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalTextSecondary
                    )
                    Text(
                        text = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "ES")).format(Date())
                            .replaceFirstChar { it.uppercase() },
                        fontSize = 15.sp,
                        color = NaturalTextSecondary
                    )
                }
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier
                        .size(46.dp)
                        .background(Color.White, CircleShape)
                        .border(1.dp, NaturalBorder, CircleShape)
                        .testTag("refresh_today_button")
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = NaturalPrimary)
                }
            }
        }

        // Spotlight / Hero Card: Next Dose to Take (Matching Natural Tones Design HTML exactly)
        item {
            if (nextPendingToma != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NaturalBorder, RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalHeroCardBackground),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp)
                    ) {
                        // Icon + Label header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(NaturalPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    text = "TOCA PARA CONFIRMAR",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalTextSecondary,
                                    letterSpacing = 1.2.sp
                                )
                                Text(
                                    text = nextPendingToma.medicamentoNombre,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = NaturalTextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large Time Display
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.padding(start = 4.dp)
                        ) {
                            Text(
                                text = nextPendingToma.horarioTexto,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.Black,
                                color = NaturalTextPrimary,
                                lineHeight = 48.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "HS",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Medium,
                                color = NaturalTextSecondary,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Large One-Tap Confirmation Button
                        SeniorButton(
                            text = "YA LO TOMÉ",
                            onClick = { onConfirmToma(nextPendingToma.id) },
                            containerColor = NaturalPrimary,
                            icon = Icons.Default.CheckCircle,
                            testTag = "hero_confirm_toma_button"
                        )
                    }
                }
            } else if (totalTomas > 0) {
                // All doses taken!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalSuccessGreen, RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalSuccessGreenContainer),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = NaturalSuccessGreen,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "¡Al día!",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalSuccessGreen
                            )
                            Text(
                                text = "Ha tomado todas las medicinas programadas para hoy.",
                                fontSize = 16.sp,
                                color = NaturalTextPrimary
                            )
                        }
                    }
                }
            } else {
                // Empty state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(32.dp)),
                    colors = CardDefaults.cardColors(containerColor = NaturalCardWhite),
                    shape = RoundedCornerShape(32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Aún no tiene medicamentos cargados",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Presione abajo para agregar su primer medicamento por voz o escribiendo.",
                            fontSize = 15.sp,
                            color = NaturalTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        SeniorButton(
                            text = "+ AGREGAR MEDICAMENTO",
                            onClick = onNavigateToAddMedication,
                            testTag = "empty_add_medication_button"
                        )
                    }
                }
            }
        }

        // Quick Action 2-Grid Section (Mis Notas / Usar Voz) from Design HTML
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Quick Card 1: Mis Medicamentos / Registro
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📋", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$confirmedCount de $totalTomas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                        Text(
                            text = "Tomas del día",
                            fontSize = 13.sp,
                            color = NaturalTextSecondary
                        )
                    }
                }

                // Quick Card 2: Usar Voz (Active Pill)
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(28.dp))
                        .clickable { onNavigateToAddMedication() }
                        .border(1.5.dp, NaturalPrimaryContainer, RoundedCornerShape(28.dp))
                        .testTag("quick_voice_action_card"),
                    colors = CardDefaults.cardColors(containerColor = NaturalActionCardBackground),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎙️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Usar Voz",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary
                        )
                        Text(
                            text = "Carga rápida",
                            fontSize = 13.sp,
                            color = NaturalTextSecondary
                        )
                    }
                }
            }
        }

        // Overdue Alert Banner (Matching Design HTML `<div class='bg-[#F1F0F4] rounded-2xl p-4 flex items-center gap-4 border-l-8 border-[#BA1A1A]'>`)
        if (overdueToma != null) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NaturalAlertContainer),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 8dp left accent stripe in NaturalAlertRed
                    Box(
                        modifier = Modifier
                            .width(8.dp)
                            .height(72.dp)
                            .background(NaturalAlertRed)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(text = "⚠️", fontSize = 26.sp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)) {
                        Text(
                            text = "ATENCIÓN / PENDIENTE",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${overdueToma.medicamentoNombre} (${overdueToma.horarioTexto} hs)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalTextPrimary
                        )
                    }
                }
            }
        }

        // Section Title: Horarios del Día
        if (totalTomas > 0) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Horarios de hoy",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )
                    Text(
                        text = "$confirmedCount/$totalTomas listas",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NaturalTextSecondary
                    )
                }
            }

            items(todayTomas, key = { it.id }) { toma ->
                SeniorTomaCard(
                    toma = toma,
                    onConfirm = { onConfirmToma(toma.id) },
                    onUnconfirm = { onUnconfirmToma(toma.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SeniorTomaCard(
    toma: TomaRecordEntity,
    onConfirm: () -> Unit,
    onUnconfirm: () -> Unit
) {
    val isConfirmed = toma.confirmado

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.5.dp,
                color = if (isConfirmed) NaturalSuccessGreen else NaturalBorder,
                shape = RoundedCornerShape(24.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfirmed) Color(0xFFF4FAF5) else Color.White
        ),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Time
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = if (isConfirmed) NaturalSuccessGreen else NaturalPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${toma.horarioTexto} hs",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isConfirmed) NaturalSuccessGreen else NaturalPrimary
                    )
                }

                SeniorStatusBadge(
                    isConfirmed = isConfirmed,
                    isDueNow = !isConfirmed && toma.fechaHoraProgramada <= System.currentTimeMillis() + (30 * 60 * 1000)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = toma.medicamentoNombre,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalTextPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (!isConfirmed) {
                SeniorButton(
                    text = "✓ YA LA TOMÉ",
                    onClick = onConfirm,
                    containerColor = NaturalSuccessGreen,
                    testTag = "confirm_toma_${toma.id}_button"
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "✓ Registrada como tomada",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalSuccessGreen
                    )
                    Button(
                        onClick = onUnconfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEEF0F7),
                            contentColor = NaturalTextSecondary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("unconfirm_toma_${toma.id}_button")
                    ) {
                        Text("Desmarcar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

