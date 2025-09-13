package com.teladanprimaagro.tmpp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teladanprimaagro.tmpp.data.UserRole
import com.teladanprimaagro.tmpp.ui.screens.harvester.DataPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.DataPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaBlokScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaKemandoranScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaKendaraanScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaPemanenScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaSupirScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.KelolaTphScreen
import com.teladanprimaagro.tmpp.ui.screens.main.LoginScreen
import com.teladanprimaagro.tmpp.ui.screens.main.MainScreen
import com.teladanprimaagro.tmpp.ui.screens.main.NfcScannerScreen
import com.teladanprimaagro.tmpp.ui.screens.harvester.PanenInputScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.PengirimanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.harvester.PetaScreen
import com.teladanprimaagro.tmpp.ui.screens.harvester.RekapPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.RekapPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.main.RoleSelectionScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.ScanInputScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.SendPrintDataScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.SpbSettingsScreen
import com.teladanprimaagro.tmpp.ui.screens.main.SplashScreen
import com.teladanprimaagro.tmpp.ui.screens.harvester.StatistikPanenScreen
import com.teladanprimaagro.tmpp.ui.screens.driver.StatistikPengirimanScreen
import com.teladanprimaagro.tmpp.ui.screens.setting.UbahFormatUniqueNoScreen
import com.teladanprimaagro.tmpp.viewmodels.PanenViewModel
import com.teladanprimaagro.tmpp.viewmodels.PengirimanViewModel
import com.teladanprimaagro.tmpp.viewmodels.SettingsViewModel
import com.teladanprimaagro.tmpp.viewmodels.SharedNfcViewModel

@Composable
fun AppNavigation(

    sharedNfcViewModel: SharedNfcViewModel
) {
    val navController = rememberNavController()
    val pengirimanViewModel: PengirimanViewModel =viewModel()
    val panenViewModel: PanenViewModel = viewModel()
    val settingsViewModel: SettingsViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash_screen") {
        composable("splash_screen") {
            SplashScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }

        composable("login_screen") {
            LoginScreen(
                navController = navController,
            )
        }

        composable("role_selection_screen") {
            RoleSelectionScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }

        composable(
            route = "main_screen/{userRole}",
            arguments = listOf(navArgument("userRole") { type = NavType.StringType })
        ) { backStackEntry ->
            val userRoleString = backStackEntry.arguments?.getString("userRole")
            val userRole = userRoleString?.let { UserRole.valueOf(it) } ?: UserRole.HARVESTER
            MainScreen(
                mainNavController = navController,
                userRole = userRole,
                settingsViewModel = settingsViewModel
            )
        }

        //Screen Harvester

        composable(
            route = "panenInputScreen/{panenId}",
            arguments = listOf(
                navArgument("panenId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val panenId = backStackEntry.arguments?.getInt("panenId") ?: -1

            val panenDataToEdit by panenViewModel.panenDataToEdit.collectAsState()

            LaunchedEffect(panenId) {
                if (panenId != -1) {
                    panenViewModel.loadPanenDataById(panenId)
                } else {
                    panenViewModel.clearPanenDataToEdit()
                }
            }

            PanenInputScreen(
                navController = navController,
                panenViewModel = panenViewModel,
                settingsViewModel = settingsViewModel,
                sharedNfcViewModel = sharedNfcViewModel,
                panenDataToEdit = if (panenId != -1) panenDataToEdit else null
            )
        }

        composable("rekap_panen_screen") {
            RekapPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }

        composable("statistik_panen_screen") {
            StatistikPanenScreen(
                navController = navController,
                panenViewModel = panenViewModel
            )
        }

        composable("data_terkirim_screen") {
            DataPanenScreen(
                navController = navController,
            )
        }

        composable("peta_screen") {
            PetaScreen(
                navController,
            )
        }

        composable("ubah_format_unique_no_screen") {
            UbahFormatUniqueNoScreen(navController, settingsViewModel
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


        //Screen Driver

        composable("scan_input_screen") {
            ScanInputScreen(
                navController = navController,
                pengirimanViewModel = pengirimanViewModel,
                sharedNfcViewModel = sharedNfcViewModel,
            )
        }
        composable("pengiriman_input_screen") {
            PengirimanInputScreen(
                navController,
                pengirimanViewModel
            )
        }
        composable("rekap_pengiriman_screen") {
            RekapPengirimanScreen(
                navController,
                pengirimanViewModel
            )
        }

        composable("data_pengiriman_screen") {
            DataPengirimanScreen(
                navController,
            )
        }

        composable("spb_settings_screen") {
            SpbSettingsScreen(
                navController,
                settingsViewModel
            )
        }

        composable("kelola_supir_screen") {
            KelolaSupirScreen(
                navController,
                settingsViewModel
            )
        }

        composable("kelola_kendaraan_screen") {
            KelolaKendaraanScreen(
                navController,
                settingsViewModel
            )
        }

        composable("statistik_pengiriman_screen") {
            StatistikPengirimanScreen(
                navController = navController,
            )
        }

        composable(
            route = "send_print_data/{pengirimanId}",
            arguments = listOf(navArgument("pengirimanId") { type = NavType.IntType })
        ) { backStackEntry ->
            val pengirimanId = backStackEntry.arguments?.getInt("pengirimanId") ?: -1
            SendPrintDataScreen(navController, pengirimanId)
        }

        composable("nfc_scanner_screen") {
            NfcScannerScreen(navController = navController, sharedNfcViewModel = sharedNfcViewModel)
        }
    }
}
