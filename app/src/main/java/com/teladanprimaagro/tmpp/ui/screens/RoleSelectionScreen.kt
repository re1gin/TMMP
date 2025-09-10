package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import com.teladanprimaagro.tmpp.ui.theme.DangerRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectionScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    var isLoading by remember { mutableStateOf(false) }
    var showPasswordHarvester by remember { mutableStateOf(false) }
    var showPasswordDriver by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .background(Color.DarkGray, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "Pilih Peran Anda",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Tombol untuk Harvester
            RoleButton(
                icon = Icons.Default.Person,
                label = "Harvester",
                onClick = { showPasswordHarvester = true },
                color = MainColor
            )

            // Tombol untuk Driver
            Spacer(modifier = Modifier.height(16.dp))
            RoleButton(
                icon = Icons.Default.LocalShipping,
                label = "Driver",
                onClick = { showPasswordDriver = true },
                color = MainColor
            )
        }

        // Dialog Password untuk Harvester
        if (showPasswordHarvester) {
            PasswordDialog(
                onDismissRequest = { showPasswordHarvester = false },
                onConfirm = { password ->
                    if (password == "panen123") {
                        isLoading = true
                        showPasswordHarvester = false
                        settingsViewModel.loginSuccess(UserRole.HARVESTER)
                        navController.navigate("main_screen/${UserRole.HARVESTER.name}") {
                            popUpTo("role_selection_screen") { inclusive = true }
                        }
                    } else {
                        // Tidak perlu logika di sini karena PasswordDialog akan menampilkannya
                    }
                },
                correctPassword = "panen123"
            )
        }

        // Dialog Password untuk Driver
        if (showPasswordDriver) {
            PasswordDialog(
                onDismissRequest = { showPasswordDriver = false },
                onConfirm = { password ->
                    if (password == "supir123") {
                        isLoading = true
                        showPasswordDriver = false
                        settingsViewModel.loginSuccess(UserRole.DRIVER)
                        navController.navigate("main_screen/${UserRole.DRIVER.name}") {
                            popUpTo("role_selection_screen") { inclusive = true }
                        }
                    } else {
                        // Tidak perlu logika di sini
                    }
                },
                correctPassword = "supir123"
            )
        }

        // Animasi Loading
        AnimatedVisibility(
            visible = isLoading,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)),
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(60.dp))
            }
        }
    }
}

@Composable
fun RoleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    color: Color
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordDialog(onDismissRequest: () -> Unit, onConfirm: (String) -> Unit, correctPassword: String) {
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isPasswordError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text(
                text = "Masukkan Password",
                color = Color.Black
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        isPasswordError = false
                    },
                    label = { Text("Password") },
                    isError = isPasswordError,
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    trailingIcon = {
                        val image = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle password visibility")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                if (isPasswordError) {
                    Text(
                        text = "Password salah. Silakan coba lagi.",
                        color = DangerRed,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (password == correctPassword) {
                        onConfirm(password)
                    } else {
                        isPasswordError = true
                    }
                }
            ) {
                Text("Konfirmasi")
            }
        },
        dismissButton = {
            Button(
                onClick = onDismissRequest
            ) {
                Text("Batal")
            }
        }
    )
}