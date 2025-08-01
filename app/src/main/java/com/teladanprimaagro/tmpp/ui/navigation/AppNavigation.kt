package com.teladanprimaagro.tmpp.ui.navigation

import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.runtime.State
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.screens.LoginScreen
import com.teladanprimaagro.tmpp.ui.screens.SplashScreen
import com.teladanprimaagro.tmpp.ui.screens.HomeScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaBlokScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaKemandoranScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaKendaraanScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaPemanenScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaSupirScreen
import com.teladanprimaagro.tmpp.ui.screens.KelolaTphScreen
import com.teladanprimaagro.tmpp.ui.screens.PanenInputScreen
import com.teladanprimaagro.tmpp.ui.screens.PengaturanScreen
import com.teladanprimaagro.tmpp.ui.screens.RekapPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.PengirimanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.RekapPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.ScanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.SendPrintDataScreen
import com.teladanprimaagro.tmpp.ui.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.ui.viewmodels.SharedNfcViewModel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(
    nfcIntent: State<Intent?>, // Ini sekarang digunakan oleh kedua dialog
    pengirimanViewModel: PengirimanViewModel,
    sharedNfcViewModel: SharedNfcViewModel
) {
    val navController = rememberNavController()
    val panenViewModel: PanenViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel() // SettingsViewModel diinstansiasi di sini

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(navController = navController, settingsViewModel = settingsViewModel)
        }
        composable("login_screen") {
            LoginScreen(navController = navController, settingsViewModel = settingsViewModel)
        }

        composable(
            route = "home_screen/{userRole}",
            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
        ) { backStackEntry ->
            val userRoleString = backStackEntry.arguments?.getString("userRole")
            val userRole = userRoleString?.let { UserRole.valueOf(it) }
                ?: UserRole.HARVESTER // Default role jika tidak ada

            HomeScreen(
                navController = navController,
                userRole = userRole
            )
        }

        composable(
            route = "send_print_data/{pengirimanId}",
            arguments = listOf(navArgument("pengirimanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pengirimanId = backStackEntry.arguments?.getInt("pengirimanId") ?: -1
            SendPrintDataScreen(navController, pengirimanId)
        }

        composable("panen_input_screen") {
            PanenInputScreen(
                navController = navController,
                panenViewModel = panenViewModel,
                settingsViewModel = settingsViewModel,
                nfcIntentFromActivity = nfcIntent,
            )
        }

        composable("rekap_panen_screen") {
            RekapPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }
        composable("pengaturan_screen") {
            val currentUserRole = settingsViewModel.getUserRole() ?: UserRole.HARVESTER // Default jika null
            PengaturanScreen(
                navController = navController,
                settingsViewModel = settingsViewModel,
                userRole = currentUserRole
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

        composable("scan_input_screen") {
            ScanInputScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel,
                sharedNfcViewModel = sharedNfcViewModel,
                nfcIntentFromActivity = nfcIntent
            )
        }
        composable("pengiriman_input_screen") {
            PengirimanInputScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel
            )
        }
        composable("rekap_pengiriman_screen") {
            RekapPengirimanScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel
            )
        }
        composable("kelola_supir_screen") {
            KelolaSupirScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable("kelola_kendaraan_screen") {
            KelolaKendaraanScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
    }
}