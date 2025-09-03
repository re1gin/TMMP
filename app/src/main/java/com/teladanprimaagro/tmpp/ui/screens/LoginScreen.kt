package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.theme.BackgroundDarkGrey
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White

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


    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_agro),
                contentDescription = "App Logo",
                modifier = Modifier.size(90.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Teladan Prima Agro",
                color = White,
                fontSize = 24.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(120.dp))

        // Input Username
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                loginError = false
            },
            label = { Text("Username") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(0.8f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = "Username Icon",
                    tint = MainColor.copy(0.8f),
                )
            },
            isError = loginError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MainColor,
                unfocusedBorderColor = White.copy(alpha = 0.38f),
                errorBorderColor = DangerRed,
                errorTextColor = DangerRed,
                focusedLabelColor = White,
                unfocusedLabelColor = White.copy(alpha = 0.6f),
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = MainColor
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Input Password
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                loginError = false
            },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(0.8f),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Sembunyikan password" else "Tampilkan password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = image,
                        contentDescription = description,
                        tint = MainColor.copy(0.8f),
                    )
                }
            },
            isError = loginError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MainColor,
                unfocusedBorderColor = White.copy(alpha = 0.38f),
                errorBorderColor = DangerRed,
                errorTextColor = DangerRed,
                focusedLabelColor = White,
                unfocusedLabelColor = White.copy(alpha = 0.6f),
                focusedTextColor = White,
                unfocusedTextColor = White,
                cursorColor = MainColor
            )
        )

        // Pesan Error
        if (loginError) {
            Text(
                text = "Username atau password salah",
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tombol Login
        Button(
            onClick = {
                val authenticatedUserRole: UserRole? = when {
                    username.trim() == "pemanen" && password.trim() == "panen123" -> UserRole.HARVESTER
                    username.trim() == "supir" && password.trim() == "supir123" -> UserRole.DRIVER
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
                containerColor = MainColor.copy(0.7f),
                contentColor = White
            )
        ) {
            Text("LOGIN", fontWeight = FontWeight.Bold)
        }
    }
}