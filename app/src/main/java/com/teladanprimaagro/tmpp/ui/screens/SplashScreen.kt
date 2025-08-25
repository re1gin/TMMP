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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.*
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.R
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    LaunchedEffect(Unit) {
        delay(3000)

        val isLoggedIn = settingsViewModel.isUserLoggedIn()
        val userRole = settingsViewModel.getUserRole()

        if (isLoggedIn && userRole != null) {
            navController.navigate("main_screen/${userRole.name}") {
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
            .background(color = MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(180.dp)
        )
    }
}