package com.teladanprimaagro.tmpp.ui.screens.main


import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.components.AppBottomBar
import com.teladanprimaagro.tmpp.ui.screens.setting.PengaturanScreen
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel

@Composable
fun MainScreen(
    mainNavController: NavHostController,
    userRole: UserRole,
    settingsViewModel: SettingsViewModel
) {
    val bottomNavController = rememberNavController()

    Scaffold(
        bottomBar = {
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
        }
    }
}
