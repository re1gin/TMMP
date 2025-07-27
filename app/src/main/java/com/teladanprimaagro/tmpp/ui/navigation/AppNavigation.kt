package com.teladanprimaagro.tmpp.ui.navigation

import android.content.Intent // Import Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.teladanprimaagro.tmpp.ui.screens.LoginScreen
import com.teladanprimaagro.tmpp.ui.screens.SplashScreen

import com.teladanprimaagro.tmpp.ui.screens.HarvesterScreen
import com.teladanprimaagro.tmpp.ui.screens.DriverScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaBlokScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaKemandoranScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaPemanenScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaTphScreen
import com.teladanprimaagro.tmpp.ui.screens.PanenInputScreen
import com.teladanprimaagro.tmpp.ui.screens.PengaturanScreen
import com.teladanprimaagro.tmpp.ui.screens.RekapPanenScreen
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import androidx.compose.runtime.State // Import State

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    // Tambahkan parameter nfcIntent di sini
    nfcIntent: State<Intent?> // Menerima State<Intent?> dari MainActivity
) {
    val navController = rememberNavController()
    val panenViewModel: PanenViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(navController = navController)
        }
        composable("login_screen") {
            LoginScreen(
                onSuccessLogin = { route ->
                    navController.navigate(route) {
                        popUpTo("login_screen") { inclusive = true }
                    }
                }
            )
        }

        composable("harvester_screen") {
            HarvesterScreen(
                navController = navController
            )
        }
        composable("driver_screen") {
            DriverScreen(
                navController = navController
            )
        }
        composable("panen_input_screen") {
            PanenInputScreen(
                navController = navController,
                panenViewModel = panenViewModel,
                settingsViewModel = settingsViewModel,
                nfcIntentFromActivity = nfcIntent // <-- TERUSKAN nfcIntent ke PanenInputScreen
            )
        }

        composable("rekap_panen_screen") {
            RekapPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }
        composable("pengaturan_screen") {
            PengaturanScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable("kelola_kemandoran_screen") {
            KelolaKemandoranScreen(
                navController, settingsViewModel
            )
        }
        composable("kelola_pemanen_screen") {
            KelolaPemanenScreen(
                navController, settingsViewModel
            )
        }
        composable("kelola_blok_screen") {
            KelolaBlokScreen(
                navController, settingsViewModel
            )
        }
        composable("kelola_tph_screen") {
            KelolaTphScreen(
                navController, settingsViewModel
            )
        }
    }
}