package com.teladanprimaagro.tmpp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel // Menerima SettingsViewModel
) {
    LaunchedEffect(Unit) {
        delay(3000) // Tunda selama 2 detik untuk efek splash

        // TODO: Implementasi logika untuk memeriksa status login dan peran pengguna
        val isLoggedIn = settingsViewModel.isUserLoggedIn() // Misal ada fungsi ini di SettingsViewModel
        val userRole = settingsViewModel.getUserRole() // Misal ada fungsi ini di SettingsViewModel

        if (isLoggedIn && userRole != null) {
            // Jika sudah login dan peran diketahui, navigasi ke HomeScreen dengan peran yang benar
            navController.navigate("home_screen/${userRole.name}") {
                popUpTo("splash_screen") { inclusive = true }
            }
        } else {
            // Jika belum login, navigasi ke LoginScreen
            navController.navigate("login_screen") {
                popUpTo("splash_screen") { inclusive = true }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black), // Latar belakang splash
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Ganti dengan logo aplikasi Anda
            contentDescription = "App Logo",
            modifier = Modifier.size(150.dp)
        )
    }
}
