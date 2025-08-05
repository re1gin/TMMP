package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.ui.theme.DotGray
import com.teladanprimaagro.tmpp.ui.theme.IconOrange
import com.teladanprimaagro.tmpp.ui.theme.TextGray
import com.teladanprimaagro.tmpp.ui.theme.BackgroundLightGray
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.data.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Bagian Atas Oranye
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.4f)
                .background(MaterialTheme.colorScheme.primary),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TMMP",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Teladan Micro Macro Program",
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
        }

        // Bagian Bawah Hitam
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // Placeholder Logo yang Disesuaikan (Lebih Sederhana)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(color = BackgroundLightGray, shape = RoundedCornerShape(50))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Logo",
                    modifier = Modifier.size(48.dp),
                    tint = IconOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Teladan Prima Agro",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = MaterialTheme.typography.headlineMedium.fontSize,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(32.dp))

            TextField(
                value = username,
                onValueChange = {
                    username = it
                    usernameError = false
                    loginError = false
                },
                label = { Text("Username") },
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(min = 56.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = "Ikon Username",
                        tint = IconOrange
                    )
                },
                isError = usernameError,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorTextColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = BackgroundLightGray,
                    unfocusedContainerColor = BackgroundLightGray,
                    disabledContainerColor = BackgroundLightGray,
                    errorContainerColor = BackgroundLightGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = if (usernameError) MaterialTheme.colorScheme.error else TextGray,
                    unfocusedLabelColor = if (usernameError) MaterialTheme.colorScheme.error else TextGray,
                    disabledLabelColor = TextGray.copy(alpha = 0.38f),
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    focusedTrailingIconColor = IconOrange,
                    unfocusedTrailingIconColor = IconOrange,
                    errorTrailingIconColor = MaterialTheme.colorScheme.error
                )
            )
            if (usernameError) {
                Text(
                    text = "Username tidak boleh kosong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = false
                    loginError = false
                },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .heightIn(min = 56.dp),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    val description = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description, tint = IconOrange)
                    }
                },
                isError = passwordError,
                colors = TextFieldDefaults.colors(
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                    errorTextColor = MaterialTheme.colorScheme.error,
                    focusedContainerColor = BackgroundLightGray,
                    unfocusedContainerColor = BackgroundLightGray,
                    disabledContainerColor = BackgroundLightGray,
                    errorContainerColor = BackgroundLightGray,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    errorCursorColor = MaterialTheme.colorScheme.error,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = MaterialTheme.colorScheme.error,
                    focusedLabelColor = if (passwordError) MaterialTheme.colorScheme.error else TextGray,
                    unfocusedLabelColor = if (passwordError) MaterialTheme.colorScheme.error else TextGray,
                    disabledLabelColor = TextGray.copy(alpha = 0.38f),
                    errorLabelColor = MaterialTheme.colorScheme.error,
                    focusedTrailingIconColor = IconOrange,
                    unfocusedTrailingIconColor = IconOrange,
                    errorTrailingIconColor = MaterialTheme.colorScheme.error
                )
            )
            if (passwordError) {
                Text(
                    text = "Password tidak boleh kosong",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tombol Login
            Button(
                onClick = {
                    usernameError = false
                    passwordError = false
                    loginError = false

                    if (username.isBlank()) {
                        usernameError = true
                    }
                    if (password.isBlank()) {
                        passwordError = true
                    }

                    if (usernameError || passwordError) {
                        loginError = true
                        return@Button
                    }

                    // Logika autentikasi dummy
                    val authenticatedUserRole: UserRole? = when {
                        username == "pemanen" && password == "panen123" -> UserRole.HARVESTER
                        username == "supir" && password == "supir123" -> UserRole.DRIVER
                        else -> null
                    }

                    if (authenticatedUserRole != null) {
                        settingsViewModel.loginSuccess(authenticatedUserRole)

                        navController.navigate("main_screen/${authenticatedUserRole.name}") {
                            popUpTo("login_screen") { inclusive = true }
                        }
                    } else {
                        loginError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("LOGIN", fontWeight = FontWeight.Bold)
            }

            if (loginError && !usernameError && !passwordError) {
                Text(
                    text = "Username atau password salah",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
