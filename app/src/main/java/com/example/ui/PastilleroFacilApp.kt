package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.ui.screens.AddMedicationDialog
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.MedicationsListScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.TodayTomasScreen
import com.example.ui.screens.UserRegistrationDialog
import com.example.ui.theme.MedicalTealContainer
import com.example.ui.theme.MedicalTealPrimary
import com.example.ui.theme.SeniorBackground
import com.example.ui.theme.SeniorBorder
import com.example.ui.theme.SeniorTextPrimary
import com.example.ui.theme.SeniorTextSecondary

data class NavItem(
    val title: String,
    val icon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PastilleroFacilApp(
    viewModel: MainViewModel = viewModel()
) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val activeMedications by viewModel.activeMedications.collectAsStateWithLifecycle()
    val todayTomas by viewModel.todayTomas.collectAsStateWithLifecycle()
    val allTomasHistory by viewModel.allTomasHistory.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    var showAddMedDialog by remember { mutableStateOf(false) }
    var showRegistrationDialog by remember { mutableStateOf(false) }

    // Check if initial registration is needed (name is blank)
    LaunchedEffect(userProfile) {
        if (userProfile != null && userProfile!!.nombre.isBlank()) {
            showRegistrationDialog = true
        }
    }

    // Show toast / snackbar for messages
    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessage()
        }
    }

    val navItems = listOf(
        NavItem("Hoy", Icons.Default.Today, "nav_tab_today"),
        NavItem("Medicinas", Icons.Default.Medication, "nav_tab_meds"),
        NavItem("Historial", Icons.Default.History, "nav_tab_history"),
        NavItem("Perfil", Icons.Default.Person, "nav_tab_profile")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = com.example.ui.theme.NaturalBackground,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Pastillero Fácil",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.NaturalPrimary,
                                letterSpacing = (-0.5).sp
                            )
                        }
                        val userName = userProfile?.nombre?.takeIf { it.isNotBlank() } ?: "Carmen"
                        Text(
                            text = "Hola, $userName",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = com.example.ui.theme.NaturalTextSecondary
                        )
                    }
                },
                actions = {
                    val photo = userProfile?.fotoPerfil
                    val initial = userProfile?.nombre?.firstOrNull()?.uppercaseChar()?.toString() ?: "C"
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.NaturalPrimaryContainer)
                            .border(2.dp, com.example.ui.theme.NaturalPrimary, CircleShape)
                            .clickable { viewModel.selectTab(3) }
                            .testTag("top_bar_profile_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!photo.isNullOrBlank()) {
                            AsyncImage(
                                model = photo,
                                contentDescription = "Perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = initial,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = com.example.ui.theme.NaturalPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = com.example.ui.theme.NaturalBackground
                ),
                modifier = Modifier
                    .border(0.dp, Color.Transparent)
                    .testTag("main_top_app_bar")
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = com.example.ui.theme.NaturalNavBackground,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .border(1.dp, com.example.ui.theme.NaturalBorder)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("senior_navigation_bar")
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { viewModel.selectTab(index) },
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                modifier = Modifier.size(26.dp)
                            )
                        },
                        label = {
                            Text(
                                text = item.title.uppercase(),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = com.example.ui.theme.NaturalPrimary,
                            selectedTextColor = com.example.ui.theme.NaturalPrimary,
                            indicatorColor = com.example.ui.theme.NaturalPrimaryContainer,
                            unselectedIconColor = com.example.ui.theme.NaturalTextSecondary,
                            unselectedTextColor = com.example.ui.theme.NaturalTextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 || selectedTab == 1) {
                ExtendedFloatingActionButton(
                    onClick = { showAddMedDialog = true },
                    icon = { Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(28.dp)) },
                    text = { Text("Agregar Medicina", fontSize = 17.sp, fontWeight = FontWeight.Bold) },
                    containerColor = com.example.ui.theme.NaturalPrimary,
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("fab_add_medication")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TodayTomasScreen(
                    userProfile = userProfile,
                    todayTomas = todayTomas,
                    onConfirmToma = { viewModel.confirmToma(it) },
                    onUnconfirmToma = { viewModel.unconfirmToma(it) },
                    onNavigateToAddMedication = { showAddMedDialog = true },
                    onRefresh = { viewModel.refreshToday() }
                )
                1 -> MedicationsListScreen(
                    medications = activeMedications,
                    onAddNewMedication = { showAddMedDialog = true },
                    onDeleteMedication = { viewModel.deleteMedication(it) }
                )
                2 -> HistoryScreen(
                    historyTomas = allTomasHistory
                )
                3 -> ProfileSettingsScreen(
                    userProfile = userProfile,
                    onSaveProfile = { name, photo, isInsistent ->
                        viewModel.saveUserProfile(name, photo, isInsistent)
                    },
                    onTestNotification = {
                        viewModel.testReminderNotification()
                    }
                )
            }
        }
    }

    // Modal: Add Medication
    if (showAddMedDialog) {
        AddMedicationDialog(
            onDismiss = { showAddMedDialog = false },
            onSave = { name, freq, startHour ->
                viewModel.addMedication(name, freq, startHour)
                showAddMedDialog = false
            }
        )
    }

    // Modal: User initial registration (Name and optional photo)
    if (showRegistrationDialog) {
        UserRegistrationDialog(
            onSave = { name, photo ->
                viewModel.saveUserProfile(name, photo, true)
                showRegistrationDialog = false
            },
            onDismiss = { showRegistrationDialog = false }
        )
    }
}
