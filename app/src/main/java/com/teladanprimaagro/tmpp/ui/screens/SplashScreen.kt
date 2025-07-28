package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import com.teladanprimaagro.tmpp.R
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.delay

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun SplashScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo Aplikasi",
            modifier = Modifier.size(200.dp)
        )
    }

    LaunchedEffect(key1 = true) {
        delay(2000L)

        if (settingsViewModel.isUserLoggedIn()) {
            val userRole = settingsViewModel.getUserRole()
            val targetRoute = when (userRole) {
                "harvester" -> "harvester_screen"
                "driver" -> "driver_screen"
                else -> "login_screen"
            }
            navController.navigate(targetRoute) {
                popUpTo("splash_screen") { inclusive = true }
            }
        } else {
            navController.navigate("login_screen") {
                popUpTo("splash_screen") { inclusive = true }
            }
        }
    }
}
