package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TomaRecordEntity
import com.example.ui.theme.NaturalAlertContainer
import com.example.ui.theme.NaturalAlertRed
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalSuccessGreen
import com.example.ui.theme.NaturalSuccessGreenContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    historyTomas: List<TomaRecordEntity>
) {
    val totalRecords = historyTomas.size
    val confirmedRecords = historyTomas.count { it.confirmado }
    val unconfirmedRecords = totalRecords - confirmedRecords

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
                text = "Historial de Tomas",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalPrimary
            )
            Text(
                text = "Registro de tomas confirmadas y no confirmadas",
                fontSize = 16.sp,
                color = NaturalTextSecondary
            )
        }

        // Summary Stats
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NaturalBorder, RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$confirmedRecords",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalSuccessGreen
                        )
                        Text(
                            text = "Confirmadas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalTextSecondary
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .width(1.dp)
                            .height(48.dp),
                        color = NaturalBorder
                    ) {}

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$unconfirmedRecords",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unconfirmedRecords > 0) NaturalAlertRed else NaturalTextSecondary
                        )
                        Text(
                            text = "No confirmadas",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = NaturalTextSecondary
                        )
                    }
                }
            }
        }

        if (historyTomas.isEmpty()) {
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Aún no hay registros de tomas",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "A medida que tome y confirme sus medicamentos, aquí aparecerá su historial.",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(historyTomas, key = { it.id }) { record ->
                HistoryItemCard(record = record)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun HistoryItemCard(record: TomaRecordEntity) {
    val isConfirmed = record.confirmado
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val scheduledDateStr = dateFormat.format(Date(record.fechaHoraProgramada))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.5.dp,
                if (isConfirmed) NaturalSuccessGreen else NaturalBorder,
                RoundedCornerShape(20.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isConfirmed) Color(0xFFF7FCF8) else Color.White
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isConfirmed) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                contentDescription = null,
                tint = if (isConfirmed) NaturalSuccessGreen else NaturalAlertRed,
                modifier = Modifier.size(36.dp)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.medicamentoNombre,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = NaturalTextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Programada: $scheduledDateStr a las ${record.horarioTexto} hs",
                    fontSize = 15.sp,
                    color = NaturalTextSecondary
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (isConfirmed) {
                    val confTimeStr = if (record.fechaHoraConfirmacion != null) {
                        timeFormat.format(Date(record.fechaHoraConfirmacion)) + " hs"
                    } else {
                        "A tiempo"
                    }
                    Surface(
                        color = NaturalSuccessGreenContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "✓ Tomada a las $confTimeStr",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalSuccessGreen
                        )
                    }
                } else {
                    Surface(
                        color = NaturalAlertContainer,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "No confirmada",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalAlertRed
                        )
                    }
                }
            }
        }
    }
}

