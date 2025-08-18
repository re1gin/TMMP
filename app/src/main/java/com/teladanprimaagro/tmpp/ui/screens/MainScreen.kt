package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.components.AppBottomBar
import com.teladanprimaagro.tmpp.viewmodels.MapViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    userRole: UserRole,
    settingsViewModel: SettingsViewModel,
    mapViewModel: MapViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            AppBottomBar(
                navController = bottomNavController,
                userRole = userRole
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = bottomNavController,
            startDestination = "home_screen"
        ) {
            composable("home_screen") {
                HomeScreen(
                    navController = mainNavController,
                    userRole = userRole,
                    paddingValues = paddingValues,
                )
            }
            composable("peta_screen") {
                PetaScreen(
                    mapViewModel = mapViewModel,
                    paddingValues = paddingValues
                )
            }
            composable("pengaturan_screen") {
                PengaturanScreen(
                    navController = mainNavController,
                    settingsViewModel = settingsViewModel,
                    userRole = userRole,
                    paddingValues = paddingValues
                )
            }
            composable("laporan_screen") {
                LaporanScreen(
                )
            }
        }
    }
}