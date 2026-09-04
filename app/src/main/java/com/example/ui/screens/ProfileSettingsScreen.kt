package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.UserProfile
import com.example.ui.components.SeniorButton
import com.example.ui.components.SeniorOutlinedButton
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary

@Composable
fun ProfileSettingsScreen(
    userProfile: UserProfile?,
    onSaveProfile: (name: String, photoUri: String?, isInsistent: Boolean) -> Unit,
    onTestNotification: () -> Unit
) {
    val context = LocalContext.current
    var name by remember(userProfile) { mutableStateOf(userProfile?.nombre ?: "") }
    var photoUriString by remember(userProfile) { mutableStateOf(userProfile?.fotoPerfil) }
    var isInsistentMode by remember(userProfile) { mutableStateOf(userProfile?.modoInsistente ?: true) }

    // Safe zero-permission Photo Picker for Android 13+ and backported
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoUriString = uri.toString()
            Toast.makeText(context, "Foto de perfil seleccionada", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher for Android 13+ POST_NOTIFICATIONS
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "Permiso de notificaciones activado", Toast.LENGTH_SHORT).show()
        }
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
            Text(
                text = "Perfil y Ajustes",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NaturalPrimary
            )
            Text(
                text = "Configure su nombre y modo de avisos",
                fontSize = 16.sp,
                color = NaturalTextSecondary
            )
        }

        // Section 1: User Profile
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Datos del Usuario",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Photo preview / selector
                    Box(
                        modifier = Modifier
                            .size(104.dp)
                            .clip(CircleShape)
                            .border(3.dp, NaturalPrimary, CircleShape)
                            .background(NaturalPrimaryContainer)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                            .testTag("profile_photo_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (photoUriString != null) {
                            AsyncImage(
                                model = photoUriString,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                            Text(
                                text = initial,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Toque el círculo para cambiar foto (opcional)",
                        fontSize = 14.sp,
                        color = NaturalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Name input
                    Text(
                        text = "Su nombre:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("Ej: Carmen Gómez", fontSize = 18.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_name_input"),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NaturalPrimary,
                            unfocusedBorderColor = NaturalBorder
                        )
                    )
                }
            }
        }

        // Section 2: Notification Behavior Setting (Business Rule: Insistent vs Simple mode)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Avisos de no confirmación",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "¿Qué debe hacer la aplicación si no confirma la toma a tiempo?",
                        fontSize = 15.sp,
                        color = NaturalTextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option 1: Modo Insistente
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (isInsistentMode) 2.dp else 1.dp,
                                color = if (isInsistentMode) NaturalPrimary else NaturalBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { isInsistentMode = true }
                            .testTag("modo_insistente_option"),
                        color = if (isInsistentMode) NaturalPrimaryContainer else Color(0xFFF9FBFA),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isInsistentMode,
                                onClick = { isInsistentMode = true },
                                colors = RadioButtonDefaults.colors(selectedColor = NaturalPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Modo insistente (Recomendado)",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isInsistentMode) NaturalPrimary else NaturalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Reintenta la notificación periódicamente si no hay confirmación, para asegurar que no olvide su medicamento.",
                                    fontSize = 15.sp,
                                    color = NaturalTextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Option 2: Modo Simple
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = if (!isInsistentMode) 2.dp else 1.dp,
                                color = if (!isInsistentMode) NaturalPrimary else NaturalBorder,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { isInsistentMode = false }
                            .testTag("modo_simple_option"),
                        color = if (!isInsistentMode) NaturalPrimaryContainer else Color(0xFFF9FBFA),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = !isInsistentMode,
                                onClick = { isInsistentMode = false },
                                colors = RadioButtonDefaults.colors(selectedColor = NaturalPrimary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Modo simple",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isInsistentMode) NaturalPrimary else NaturalTextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Notifica una sola vez a la hora programada. Si no se confirma, queda marcado como \"no confirmado\" en el historial.",
                                    fontSize = 15.sp,
                                    color = NaturalTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 3: Notification Test and Permission
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Text(
                        text = "Comprobar Notificaciones",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = NaturalTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Pruebe el timbre y vibración para verificar que su teléfono le avise correctamente.",
                        fontSize = 15.sp,
                        color = NaturalTextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    SeniorOutlinedButton(
                        text = "🔔 PROBAR ALARMA Y SONIDO",
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            onTestNotification()
                        },
                        icon = Icons.Default.Notifications,
                        testTag = "test_notification_button"
                    )
                }
            }
        }

        // Save Button
        item {
            SeniorButton(
                text = "GUARDAR CAMBIOS",
                onClick = {
                    onSaveProfile(name, photoUriString, isInsistentMode)
                },
                icon = Icons.Default.Save,
                containerColor = NaturalPrimary,
                testTag = "save_profile_button"
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

