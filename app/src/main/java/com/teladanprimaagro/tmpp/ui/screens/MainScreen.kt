package com.teladanprimaagro.tmpp.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.components.AppBottomBar
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    mainNavController: NavHostController,
    userRole: UserRole,
    settingsViewModel: SettingsViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
            // Memastikan Bottom Bar hanya menampilkan Home dan Pengaturan
            AppBottomBar(
                navController = bottomNavController
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
            // Hapus composable "peta_screen" dan "laporan_screen"
            composable("pengaturan_screen") {
                PengaturanScreen(
                    navController = mainNavController,
                    settingsViewModel = settingsViewModel,
                    userRole = userRole,
                    paddingValues = paddingValues
                )
            }
        }
    }
}
