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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.BuildConfig
import com.example.data.UserProfile
import com.example.ui.components.SeniorButton
import com.example.ui.components.SeniorOutlinedButton
import com.example.ui.theme.NaturalBackground
import com.example.ui.theme.NaturalBorder
import com.example.ui.theme.NaturalPrimary
import com.example.ui.theme.NaturalPrimaryContainer
import com.example.ui.theme.NaturalTextPrimary
import com.example.ui.theme.NaturalTextSecondary
import kotlinx.coroutines.delay
import java.util.Calendar

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

    // Automatic saving for name input with gentle debounce
    LaunchedEffect(name) {
        if (userProfile != null && name != userProfile.nombre) {
            delay(400)
            val finalName = if (name.isBlank()) "Usuario" else name.trim()
            onSaveProfile(finalName, photoUriString, isInsistentMode)
        }
    }

    // Safe zero-permission Photo Picker for Android 13+ and backported
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val uriStr = uri.toString()
            photoUriString = uriStr
            // Auto-save immediately upon photo selection
            val finalName = if (name.isBlank()) "Usuario" else name.trim()
            onSaveProfile(finalName, uriStr, isInsistentMode)
            Toast.makeText(context, "Foto de perfil actualizada", Toast.LENGTH_SHORT).show()
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
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

                // Visual confirmation badge that automatic saving is active
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-guardado",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }
            }
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Datos del Usuario",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                        Text(
                            text = "Se guarda solo",
                            fontSize = 13.sp,
                            color = NaturalPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

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
                        text = "Toque el círculo para cambiar foto (se guarda automáticamente)",
                        fontSize = 14.sp,
                        color = NaturalTextSecondary,
                        textAlign = TextAlign.Center
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
                        onValueChange = { newName ->
                            name = newName
                        },
                        placeholder = { Text("Ej: Carmen Gómez", fontSize = 18.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("user_name_input"),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = androidx.compose.ui.text.TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val finalName = if (name.isBlank()) "Usuario" else name.trim()
                            onSaveProfile(finalName, photoUriString, isInsistentMode)
                        }),
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
                            .clickable {
                                isInsistentMode = true
                                val finalName = if (name.isBlank()) "Usuario" else name.trim()
                                onSaveProfile(finalName, photoUriString, true)
                            }
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
                                onClick = {
                                    isInsistentMode = true
                                    val finalName = if (name.isBlank()) "Usuario" else name.trim()
                                    onSaveProfile(finalName, photoUriString, true)
                                },
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
                            .clickable {
                                isInsistentMode = false
                                val finalName = if (name.isBlank()) "Usuario" else name.trim()
                                onSaveProfile(finalName, photoUriString, false)
                            }
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
                                onClick = {
                                    isInsistentMode = false
                                    val finalName = if (name.isBlank()) "Usuario" else name.trim()
                                    onSaveProfile(finalName, photoUriString, false)
                                },
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

        // Section 4: App Information & Developer (Dynamic Version + Developer with Current Year)
        item {
            val currentYear = remember { Calendar.getInstance().get(Calendar.YEAR) }
            val developerEmail = "sanchezluys@gmail.com"
            val dynamicVersion = remember(context) {
                try {
                    val pInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getPackageInfo(
                            context.packageName,
                            android.content.pm.PackageManager.PackageInfoFlags.of(0)
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        context.packageManager.getPackageInfo(context.packageName, 0)
                    }
                    pInfo.versionName ?: BuildConfig.VERSION_NAME
                } catch (e: Exception) {
                    BuildConfig.VERSION_NAME
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, NaturalBorder, RoundedCornerShape(28.dp))
                    .testTag("app_info_card"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(28.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NaturalPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Información de la Aplicación",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Aplicación:",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary
                        )
                        Text(
                            text = "Pastillero Fácil",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Dynamic version row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Versión:",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary
                        )
                        Surface(
                            color = NaturalPrimaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "v$dynamicVersion",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = NaturalPrimary,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Developer row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Desarrollador:",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary
                        )
                        Text(
                            text = developerEmail,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Current year row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Año actual:",
                            fontSize = 16.sp,
                            color = NaturalTextSecondary
                        )
                        Text(
                            text = "$currentYear",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = NaturalTextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "© $currentYear $developerEmail. Todos los derechos reservados.",
                        fontSize = 13.sp,
                        color = NaturalTextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Auto-save reassurance action button
        item {
            SeniorButton(
                text = "✓ DATOS GUARDADOS AUTOMÁTICAMENTE",
                onClick = {
                    val finalName = if (name.isBlank()) "Usuario" else name.trim()
                    onSaveProfile(finalName, photoUriString, isInsistentMode)
                    Toast.makeText(context, "Los datos ya están guardados automáticamente", Toast.LENGTH_SHORT).show()
                },
                icon = Icons.Default.Check,
                containerColor = NaturalPrimary,
                testTag = "save_profile_button"
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

