package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PengaturanScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    // State untuk mengontrol dialog konfirmasi sandi
    var showPasswordDialog by remember { mutableStateOf(false) }
    // State untuk menyimpan rute navigasi tujuan setelah konfirmasi sandi
    var pendingNavigationRoute by remember { mutableStateOf<String?>(null) }

    // Sandi yang akan digunakan untuk konfirmasi (GANTI DENGAN MEKANISME KEAMANAN SESUNGGUHNYA!)
    val adminPassword = "supersawit2025" // Contoh sandi admin

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header Section (Back, Title, Settings)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Text(
                text = "Pengaturan",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Main Content Area (Rounded Black Section)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Menu Item: Tema (bisa jadi tidak perlu sandi)
            SettingMenuItem(text = "Tema") {
                // TODO: Aksi untuk mengubah tema (misalnya, menampilkan dialog pilihan tema)
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Kemandoran (perlu sandi)
            SettingMenuItem(text = "Kemandoran") {
                pendingNavigationRoute = "kelola_kemandoran_screen"
                showPasswordDialog = true
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Pemanen (perlu sandi)
            SettingMenuItem(text = "Pemanen") {
                pendingNavigationRoute = "kelola_pemanen_screen"
                showPasswordDialog = true
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: Blok (perlu sandi)
            SettingMenuItem(text = "Blok") {
                pendingNavigationRoute = "kelola_blok_screen"
                showPasswordDialog = true
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Menu Item: No. TPH (perlu sandi)
            SettingMenuItem(text = "No. TPH") {
                pendingNavigationRoute = "kelola_tph_screen"
                showPasswordDialog = true
            }
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.weight(1f)) // Dorong footer ke bawah

            // Menu Item: Logout (tidak perlu sandi karena ini tindakan keluar)
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
            HorizontalDivider(thickness = 0.5.dp, color = DotGray.copy(alpha = 0.3f))

            // Version and Dots (Footer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp, top = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(DotGray, shape = RoundedCornerShape(50))
                            .padding(horizontal = 4.dp)
                    )
                    if (it < 2) Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Text(
                text = "Version: V 1.0.0.0",
                color = TextGray,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
    }

    if (showPasswordDialog) {
        PasswordConfirmationDialog(
            onConfirm = { enteredPassword ->
                if (enteredPassword == adminPassword) { // Bandingkan dengan sandi admin
                    pendingNavigationRoute?.let { route ->
                        navController.navigate(route)
                    }
                    showPasswordDialog = false
                    pendingNavigationRoute = null
                } else {
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

// Composable untuk Dialog Konfirmasi Sandi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordConfirmationDialog(
    onConfirm: (String) -> Unit, // Mengirim sandi yang dimasukkan
    onDismiss: () -> Unit
) {
    var passwordInput by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) } // State untuk menampilkan error

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
                    isError = showError, // Terapkan error state ke TextField
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
                        showError = true
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