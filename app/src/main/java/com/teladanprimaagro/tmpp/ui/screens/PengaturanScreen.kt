package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.data.UserRole

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

    val adminPassword = "supersawit2025"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pengaturan",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(16.dp) // Tambahkan padding di sini untuk konten utama
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            SettingMenuItem(text = "Tema") {
                // Aksi untuk Tema
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Gunakan `userRole?.let` untuk menangani kasus null
            userRole?.let { role ->
                when (role) {
                    UserRole.HARVESTER -> {

                        SettingMenuItem(text = "Format Nomor Unik") {
                            pendingNavigationRoute = "ubah_format_unique_no_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "Kemandoran") {
                            pendingNavigationRoute = "kelola_kemandoran_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "Pemanen") {
                            pendingNavigationRoute = "kelola_pemanen_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "Blok") {
                            pendingNavigationRoute = "kelola_blok_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "No. TPH") {
                            pendingNavigationRoute = "kelola_tph_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))
                    }
                    UserRole.DRIVER -> {
                        SettingMenuItem(text = "Nama Supir") {
                            pendingNavigationRoute = "kelola_supir_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "Nomor Polisi") {
                            pendingNavigationRoute = "kelola_kendaraan_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

                        SettingMenuItem(text = "Pengaturan SPB") {
                            pendingNavigationRoute = "spb_settings_screen"
                            showPasswordDialog = true
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))
                    }
                }
            } ?: run {
                // Opsional: Tampilkan pesan atau nonaktifkan item jika userRole null
                Text(
                    text = "Peran pengguna tidak ditemukan.",
                    modifier = Modifier.padding(16.dp),
                    color = TextGray
                )
            }


            Spacer(modifier = Modifier.weight(1f))

            SettingMenuItem(
                text = "Logout",
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                iconTint = MaterialTheme.colorScheme.error
            ) {
                settingsViewModel.logout()
                navController.navigate("login_screen") {
                    popUpTo(navController.graph.id) {
                        inclusive = true
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        PasswordConfirmationDialog(
            onConfirm = { enteredPassword ->
                if (enteredPassword == adminPassword) {
                    pendingNavigationRoute?.let { route ->
                        navController.navigate(route)
                    }
                    showPasswordDialog = false
                    pendingNavigationRoute = null
                }
            },
            onDismiss = {
                showPasswordDialog = false
                pendingNavigationRoute = null
            }
        )
    }
}

@Composable
fun SettingMenuItem(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
    iconTint: Color = TextGray,
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
            color = MaterialTheme.colorScheme.onSurface,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordConfirmationDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfirmasi Sandi") },
        text = {
            Column {
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = {
                        passwordInput = it
                        showError = false // Reset error saat input berubah
                    },
                    label = { Text("Sandi Admin") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible)
                            Icons.Filled.Visibility
                        else Icons.Filled.VisibilityOff

                        val description = if (passwordVisible) "Sembunyikan sandi" else "Tampilkan sandi"

                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = description)
                        }
                    },
                    isError = showError,
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text(
                        text = "Sandi salah",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (passwordInput == "supersawit2025") {
                        onConfirm(passwordInput)
                    } else {
                        showError = true // Set error jika sandi salah
                    }
                }
            ) {
                Text("Konfirmasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
