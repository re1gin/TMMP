package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.components.PasswordDialog
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel,
    userRole: UserRole?,
    paddingValues: PaddingValues
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var pendingNavigationRoute by remember { mutableStateOf<String?>(null) }
    val adminPassword = "1"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Pengaturan",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MainColor
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = Modifier.padding(paddingValues)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MainBackground)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)

                userRole?.let { role ->
                    when (role) {
                        UserRole.HARVESTER -> {
                            SettingMenuItem(text = "Format Nomor Unik") {
                                pendingNavigationRoute = "ubah_format_unique_no_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "Kemandoran") {
                                pendingNavigationRoute = "kelola_kemandoran_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "Pemanen") {
                                pendingNavigationRoute = "kelola_pemanen_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "Blok") {
                                pendingNavigationRoute = "kelola_blok_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "No. TPH") {
                                pendingNavigationRoute = "kelola_tph_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                        }
                        UserRole.DRIVER -> {
                            SettingMenuItem(text = "Nama Supir") {
                                pendingNavigationRoute = "kelola_supir_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "Nomor Polisi") {
                                pendingNavigationRoute = "kelola_kendaraan_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                            SettingMenuItem(text = "Pengaturan SPB") {
                                pendingNavigationRoute = "spb_settings_screen"
                                showPasswordDialog = true
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                } ?: run {
                    Text(
                        text = "Peran pengguna tidak ditemukan.",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                SettingMenuItem(
                    text = "Keluar Aplikasi",
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    iconTint = DangerRed
                ) {
                    settingsViewModel.logout()
                    navController.navigate("role_selection_screen") {
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            onDismissRequest = { showPasswordDialog = false},
            onConfirm = { password ->
                if (password == adminPassword) {
                    pendingNavigationRoute?.let { route ->
                        navController.navigate(route)
                    }
                }
            },
            correctPassword = adminPassword
        )
    }
}

@Composable
fun SettingMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = iconTint
        )
    }
}