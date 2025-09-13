package com.teladanprimaagro.tmpp.ui.screens.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.ui.theme.DangerRed
import com.teladanprimaagro.tmpp.ui.theme.MainBackground
import com.teladanprimaagro.tmpp.ui.theme.MainColor
import com.teladanprimaagro.tmpp.ui.theme.White

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var loginError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("Email atau password salah") }
    var isLoading by remember { mutableStateOf(false) }
    val firebaseAuth = FirebaseAuth.getInstance()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MainBackground),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.logo_agro),
                contentDescription = "Logo Aplikasi",
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

        Spacer(modifier = Modifier.height(40.dp))

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

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                isLoading = true
                loginError = false
                firebaseAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener { task ->
                        isLoading = false
                        if (task.isSuccessful) {
                            navController.navigate("role_selection_screen") {
                                popUpTo("login_screen") { inclusive = true }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        loginError = true
                        errorMessage = when (exception) {
                            is FirebaseAuthInvalidUserException -> "Akun tidak ditemukan atau telah dihapus."
                            is FirebaseAuthInvalidCredentialsException -> "Password yang Anda masukkan salah."
                            else -> "Terjadi kesalahan saat login. Periksa koneksi internet Anda."
                        }
                    }
            },
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MainColor.copy(0.7f),
                contentColor = White
            ),
            enabled = !isLoading // Nonaktifkan tombol saat loading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = White,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Login", fontWeight = FontWeight.Bold)
            }
        }

        if (loginError) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                color = DangerRed,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}